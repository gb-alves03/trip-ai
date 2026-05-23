package com.trip_ai.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trip_ai.domain.enums.BotState;
import com.trip_ai.domain.model.ConversationFsm;
import com.trip_ai.domain.model.TravelPreference;
import com.trip_ai.domain.model.User;
import com.trip_ai.domain.port.in.HandleMessageUseCase;
import com.trip_ai.domain.port.out.*;
import com.trip_ai.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService implements HandleMessageUseCase {

    private final UserRepositoryPort userPort;
    private final ConversationRepositoryPort convPort;
    private final TravelPreferenceRepositoryPort prefPort;
    private final MessagingPort messaging;
    private final IataResolverPort iataResolver;
    private final ObjectMapper                mapper;

    // ─────────────────────────────────────────────────────────────
    // Primary port implementation
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void handle(String phone, String text, String pushName) {
        User user            = getOrCreateUser(phone, pushName);
        ConversationFsm fsm  = getOrCreateFsm(user);
        ObjectNode ctx       = parseCtx(fsm.getContext());

        log.info("[FSM] tel={} estado={} msg={}", phone, fsm.getState(), text);

        switch (fsm.getState()) {
            case IDLE                      -> onIdle(user, fsm, ctx);
            case WAITING_NAME              -> onName(user, fsm, ctx, text);
            case WAITING_ORIGIN            -> onOrigin(user, fsm, ctx, text);
            case WAITING_DESTINATION       -> onDestination(user, fsm, ctx, text);
            case WAITING_DATES             -> onDates(user, fsm, ctx, text);
            case WAITING_BUDGET            -> onBudget(user, fsm, ctx, text);
            case WAITING_DIRECT_PREFERENCE -> onDirectPref(user, fsm, ctx, text);
            case AWAITING_CONFIRMATION     -> onConfirmation(user, fsm, ctx, text);
            case ACTIVE, PAUSED            -> onActiveUser(user, fsm, ctx, text);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // State handlers
    // ─────────────────────────────────────────────────────────────

    private void onIdle(User user, ConversationFsm fsm, ObjectNode ctx) {
        send(user, """
            Olá! 👋 Sou o *trip.ai*.

            Vou monitorar passagens e te avisar quando aparecer uma boa promoção — sem você precisar pesquisar em nenhum site. ✈️

            Para começar, qual é o seu nome?""");
        transition(fsm, ctx, BotState.WAITING_NAME);
    }

    private void onName(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        user.setName(text.trim());
        userPort.save(user);
        ctx.put("name", text.trim());

        send(user, "Prazer, *" + text.trim() + "*! 😄\n\n"
            + "De qual cidade você vai partir? Pode colocar o nome da cidade ou o código do aeroporto *(ex: GRU, BSB, REC, POA)*.");
        transition(fsm, ctx, BotState.WAITING_ORIGIN);
    }

    private void onOrigin(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        ctx.put("origin_raw", text.trim());
        send(user, "Ótimo! E qual é o *destino* que você quer monitorar? 🌍");
        transition(fsm, ctx, BotState.WAITING_DESTINATION);
    }

    private void onDestination(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        ctx.put("destination_raw", text.trim());
        send(user, """
            Perfeito! Você tem algum *período* em mente? 📅

            Exemplos: _julho_, _agosto a setembro_, _10/07 a 20/07_
            Se ainda não sabe, responda *flexível*.""");
        transition(fsm, ctx, BotState.WAITING_DATES);
    }

    private void onDates(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        ctx.put("dates_raw", text.trim());
        send(user, "Entendido! Qual é o seu *orçamento máximo* por pessoa (ida e volta)? 💰\n\nExemplo: _R$ 2.000_ ou _2500_");
        transition(fsm, ctx, BotState.WAITING_BUDGET);
    }

    private void onBudget(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        ctx.put("budget_raw", text.trim());
        send(user, "Última pergunta: prefere voo *direto* ou aceita *escala*? ✈️\n\nResponda: _direto_ ou _aceito escala_");
        transition(fsm, ctx, BotState.WAITING_DIRECT_PREFERENCE);
    }

    private void onDirectPref(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        boolean directOnly = text.toLowerCase().contains("direto");
        ctx.put("direct_only", directOnly);

        send(user, "Aqui está o seu perfil:\n\n"
            + buildSummary(ctx)
            + "\n\nConfirma? Responda *sim* ou *não* para corrigir.");
        transition(fsm, ctx, BotState.AWAITING_CONFIRMATION);
    }

    private void onConfirmation(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        if (text.toLowerCase().trim().startsWith("s")) {
            savePreference(user, ctx);
            user.setStatus("ACTIVE");
            userPort.save(user);

            send(user, """
                ✅ *Monitoramento ativado!*

                Vou verificar as passagens a cada 6 horas e te avisar quando encontrar uma boa promoção. 🎉

                Pode ficar tranquilo — eu cuido disso por você.
                Digite *ajuda* a qualquer momento para ver o que posso fazer.""");
            transition(fsm, mapper.createObjectNode(), BotState.ACTIVE);

        } else {
            send(user, "Sem problema! Vamos recomeçar.\n\nDe qual cidade você vai partir?");
            transition(fsm, mapper.createObjectNode(), BotState.WAITING_ORIGIN);
        }
    }

    private void onActiveUser(User user, ConversationFsm fsm, ObjectNode ctx, String text) {
        String lower = text.toLowerCase().trim();

        if (lower.equals("ajuda") || lower.equals("help") || lower.equals("menu")) {
            send(user, helpMessage());

        } else if (lower.contains("pausar") || lower.contains("parar alertas")) {
            user.setStatus("PAUSED");
            userPort.save(user);
            transition(fsm, ctx, BotState.PAUSED);
            send(user, "⏸️ Alertas pausados. Para retomar, envie *retomar*.");

        } else if (lower.contains("retomar") || lower.contains("ativar alertas")) {
            user.setStatus("ACTIVE");
            userPort.save(user);
            transition(fsm, ctx, BotState.ACTIVE);
            send(user, "▶️ Alertas reativados! Estou de olho nas passagens.");

        } else if (lower.contains("cancelar") || lower.contains("excluir minha conta")) {
            user.setStatus("CANCELLED");
            userPort.save(user);
            prefPort.deactivateByUserId(user.getId());
            transition(fsm, mapper.createObjectNode(), BotState.IDLE);
            send(user, "Ok, monitoramento encerrado. Se quiser recomeçar, é só me chamar! 👋");

        } else if (lower.contains("minhas rotas") || lower.contains("meu perfil") || lower.contains("preferências")) {
            sendProfile(user);

        } else if (lower.contains("novo destino") || lower.contains("mudar destino")) {
            send(user, "Vamos adicionar um novo destino! De qual cidade você vai partir?");
            transition(fsm, mapper.createObjectNode(), BotState.WAITING_ORIGIN);

        } else {
            send(user, "Não entendi muito bem. 😅 Digite *ajuda* para ver o que posso fazer.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private void sendProfile(User user) {
        List<TravelPreference> prefs = prefPort.findActiveByUserId(user.getId());
        if (prefs.isEmpty()) {
            send(user, "Você ainda não tem rotas configuradas. Digite *novo destino* para começar!");
            return;
        }
        var sb = new StringBuilder("📋 *Suas rotas monitoradas:*\n\n");
        for (TravelPreference p : prefs) {
            sb.append("✈️ *").append(p.getOriginIata()).append(" → ").append(p.getDestinationIata()).append("*\n");
            if (p.getDateFrom() != null)  sb.append("📅 ").append(p.getDateFrom()).append(" a ").append(p.getDateTo()).append("\n");
            if (p.getMaxBudget() != null) sb.append("💰 Até R$ ").append(p.getMaxBudget().toPlainString()).append("\n");
            sb.append(p.isDirectOnly() ? "✅ Somente direto\n" : "🔄 Aceita escala\n");
            sb.append("\n");
        }
        send(user, sb.toString().trim());
    }

    private String buildSummary(ObjectNode ctx) {
        return "✈️ *" + g(ctx, "origin_raw") + " → " + g(ctx, "destination_raw") + "*\n"
             + "📅 " + g(ctx, "dates_raw") + "\n"
             + "💰 " + g(ctx, "budget_raw") + "\n"
             + (ctx.path("direct_only").asBoolean(false) ? "✅ Somente voo direto" : "🔄 Aceita escalas");
    }

    private String helpMessage() {
        return """
            🤖 *O que posso fazer por você:*

            • *minhas rotas* — ver destinos monitorados
            • *novo destino* — adicionar ou mudar rota
            • *pausar* — pausar alertas temporariamente
            • *retomar* — reativar alertas
            • *cancelar* — encerrar monitoramento
            • *ajuda* — exibir este menu""";
    }

    private void savePreference(User user, ObjectNode ctx) {
        prefPort.deactivateByUserId(user.getId());
        prefPort.save(TravelPreference.builder()
            .user(user)
            .originIata(iataResolver.resolve(g(ctx, "origin_raw")))
            .destinationIata(iataResolver.resolve(g(ctx, "destination_raw")))
            .flexibleDates(true)
            .directOnly(ctx.path("direct_only").asBoolean(false))
            .active(true)
            .build());
    }

    private User getOrCreateUser(String phone, String pushName) {
        return userPort.findByPhone(phone).orElseGet(() ->
            userPort.save(User.builder().phone(phone).name(pushName).status("ONBOARDING").build())
        );
    }

    private ConversationFsm getOrCreateFsm(User user) {
        return convPort.findByUserId(user.getId()).orElseGet(() ->
            convPort.save(ConversationFsm.builder().userId(user.getId()).build())
        );
    }

    private ObjectNode parseCtx(String json) {
        try {
            return (ObjectNode) mapper.readTree(json == null || json.isBlank() ? "{}" : json);
        } catch (Exception e) {
            return mapper.createObjectNode();
        }
    }

    private void transition(ConversationFsm fsm, ObjectNode ctx, BotState next) {
        fsm.setState(next);
        try { fsm.setContext(mapper.writeValueAsString(ctx)); } catch (Exception e) { fsm.setContext("{}"); }
        convPort.save(fsm);
    }

    private void send(User user, String text) {
        messaging.sendText(user.getPhone(), text);
    }

    private String g(ObjectNode ctx, String key) {
        return ctx.has(key) ? ctx.get(key).asText() : "?";
    }
}

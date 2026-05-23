package com.trip_ai.app;

import com.trip_ai.domain.model.FlightOffer;
import com.trip_ai.domain.model.PriceAlert;
import com.trip_ai.domain.model.PriceHistory;
import com.trip_ai.domain.model.TravelPreference;
import com.trip_ai.domain.port.in.MonitorFlightsUseCase;
import com.trip_ai.domain.port.out.*;
import com.trip_ai.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightMonitorScheduler implements MonitorFlightsUseCase {

    private final TravelPreferenceRepositoryPort prefPort;
    private final PriceHistoryRepositoryPort historyPort;
    private final PriceAlertRepositoryPort alertPort;
    private final FlightSearchPort flightSearch;
    private final MessagingPort messaging;

    @Value("${tripai.alert.min-drop-percentage:15.0}")
    private double minDropPercentage;

    @Scheduled(cron = "${tripai.scheduler.cron:0 0 */6 * * *}")
    public void run() {
        monitor();
    }

    @Override
    public void monitor() {
        log.info("═══ Ciclo de monitoramento iniciado ═══");
        List<TravelPreference> active = prefPort.findAllActive();
        log.info("Monitorando {} rotas ativas", active.size());

        for (TravelPreference pref : active) {
            try {
                process(pref);
            } catch (Exception e) {
                log.error("Erro ao processar rota {} → {}: {}",
                    pref.getOriginIata(), pref.getDestinationIata(), e.getMessage());
            }
        }
        log.info("═══ Ciclo de monitoramento concluído ═══");
    }

    private void process(TravelPreference pref) {
        List<FlightOffer> offers = flightSearch.search(
            pref.getOriginIata(),
            pref.getDestinationIata(),
            pref.getDateFrom(),
            pref.getDateTo(),
            pref.getPassengers()
        );

        if (offers.isEmpty()) return;

        offers.forEach(offer -> historyPort.save(PriceHistory.builder()
            .origin(pref.getOriginIata())
            .destination(pref.getDestinationIata())
            .departureDate(offer.getDepartureDate())
            .price(offer.getPrice())
            .airline(offer.getAirline())
            .stops(offer.getStops())
            .affiliateUrl(offer.getAffiliateUrl())
            .build()));

        FlightOffer best  = offers.get(0);
        BigDecimal price  = best.getPrice();

        if (shouldAlertByBudget(pref, price)) {
            sendAlert(pref, best, null, null, "abaixo do seu orçamento");
            return;
        }

        BigDecimal avg = historyPort.getAvg60Days(pref.getOriginIata(), pref.getDestinationIata());
        if (avg != null && avg.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal drop = avg.subtract(price).divide(avg, 4, RoundingMode.HALF_UP);
            if (drop.doubleValue() >= minDropPercentage / 100.0) {
                sendAlert(pref, best, avg, drop, null);
            }
        }
    }

    private boolean shouldAlertByBudget(TravelPreference pref, BigDecimal price) {
        return pref.getMaxBudget() != null && price.compareTo(pref.getMaxBudget()) <= 0;
    }

    private void sendAlert(TravelPreference pref, FlightOffer offer,
                           BigDecimal avg, BigDecimal drop, String reason) {
        BigDecimal dropPct = drop != null
            ? drop.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)
            : null;

        messaging.sendText(pref.getUser().getPhone(), buildAlertMessage(pref, offer, avg, dropPct, reason));

        alertPort.save(PriceAlert.builder()
            .preferenceId(pref.getId())
            .priceFound(offer.getPrice())
            .priceAvgReference(avg)
            .dropPercentage(dropPct)
            .airline(offer.getAirline())
            .departureDate(offer.getDepartureDate())
            .affiliateUrl(offer.getAffiliateUrl())
            .build());

        log.info("Alerta enviado | tel={} | rota={} → {} | preço=R${}",
            pref.getUser().getPhone(), pref.getOriginIata(), pref.getDestinationIata(), offer.getPrice());
    }

    private String buildAlertMessage(TravelPreference pref, FlightOffer offer,
                                     BigDecimal avg, BigDecimal dropPct, String reason) {
        var sb = new StringBuilder();
        sb.append("🔥 *PROMOÇÃO ENCONTRADA!*\n\n");
        sb.append("✈️ *").append(pref.getOriginIata()).append(" → ").append(pref.getDestinationIata()).append("*\n\n");

        if (avg != null && dropPct != null) {
            sb.append("Era: ~R$ ").append(avg.setScale(0, RoundingMode.HALF_UP)).append("\n");
        }
        sb.append("Agora: *R$ ").append(offer.getPrice().setScale(0, RoundingMode.HALF_UP)).append("*");

        if (dropPct != null) {
            sb.append(" ↘️ ").append(dropPct).append("% mais barato que a média recente");
        } else if (reason != null) {
            sb.append(" — ").append(reason);
        }
        sb.append("\n\n");

        if (offer.getAirline() != null)      sb.append("🏢 ").append(offer.getAirline()).append("\n");
        if (offer.getDepartureDate() != null) sb.append("📅 Partida: ").append(offer.getDepartureDate()).append("\n");
        sb.append("🔄 ").append(offer.getStops() == 0 ? "Voo direto" : offer.getStops() + " escala(s)").append("\n\n");

        if (offer.getAffiliateUrl() != null) {
            sb.append("👉 *Reservar agora:*\n").append(offer.getAffiliateUrl()).append("\n\n");
        }
        sb.append("_Responda_ *pausar* _para não receber alertas por 7 dias._");
        return sb.toString();
    }
}

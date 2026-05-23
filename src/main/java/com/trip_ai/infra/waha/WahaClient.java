package com.trip_ai.infra.waha;

import com.trip_ai.domain.port.out.MessagingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class WahaClient implements MessagingPort {

    private final WebClient webClient;

    @Value("${tripai.waha.session:default}")
    private String session;

    public WahaClient(@Qualifier("wahaWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public void sendText(String phone, String text) {
        String chatId = phone.contains("@") ? phone : phone + "@c.us";
        var body = Map.of("chatId", chatId, "text", text, "session", session);

        webClient.post()
            .uri("/api/sendText")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                resp -> log.debug("Mensagem enviada para {}", phone),
                err  -> log.error("Falha ao enviar mensagem para {}: {}", phone, err.getMessage())
            );
    }
}

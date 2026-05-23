package com.trip_ai.web;

import com.trip_ai.domain.port.in.HandleMessageUseCase;
import com.trip_ai.infra.waha.dto.WahaWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final HandleMessageUseCase handleMessage;

    @PostMapping("/waha")
    public ResponseEntity<Void> handle(@RequestBody WahaWebhookEvent event) {
        if (event == null || !"message".equals(event.getEvent())) {
            return ResponseEntity.ok().build();
        }

        WahaWebhookEvent.Payload payload = event.getPayload();
        if (payload == null || payload.isFromMe() || payload.isGroupMessage()) {
            return ResponseEntity.ok().build();
        }

        String phone = payload.extractPhone();
        String text  = payload.getBody();

        if (phone == null || text == null || text.isBlank()) {
            return ResponseEntity.ok().build();
        }

        log.info("Mensagem recebida | tel={} | texto={}", phone, text.substring(0, Math.min(50, text.length())));
        handleMessage.handle(phone, text.trim(), payload.getNotifyName());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("trip.ai online");
    }
}

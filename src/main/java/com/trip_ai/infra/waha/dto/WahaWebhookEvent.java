package com.trip_ai.infra.waha.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WahaWebhookEvent {

    private String event;
    private String session;
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private String from;
        private boolean fromMe;
        private String body;
        private String notifyName;
        private boolean hasMedia;

        public boolean isGroupMessage() {
            return from != null && from.contains("@g.us");
        }

        public String extractPhone() {
            if (from == null) return null;
            int at = from.indexOf('@');
            return at > 0 ? from.substring(0, at) : from;
        }
    }
}

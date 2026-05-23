package com.trip_ai.domain.port.out;

public interface MessagingPort {
    void sendText(String phone, String text);
}

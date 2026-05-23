package com.trip_ai.domain.port.in;

public interface HandleMessageUseCase {
    void handle(String phone, String text, String pushName);
}

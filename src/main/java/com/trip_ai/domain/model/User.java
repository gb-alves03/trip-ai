package com.trip_ai.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class User {
    private UUID id;
    private String phone;
    private String name;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}

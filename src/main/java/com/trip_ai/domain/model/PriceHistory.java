package com.trip_ai.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PriceHistory {
    private UUID id;
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private BigDecimal price;
    private String airline;
    private int stops;
    private String affiliateUrl;
}

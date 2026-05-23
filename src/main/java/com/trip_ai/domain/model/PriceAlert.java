package com.trip_ai.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PriceAlert {
    private UUID id;
    private UUID preferenceId;
    private BigDecimal priceFound;
    private BigDecimal priceAvgReference;
    private BigDecimal dropPercentage;
    private String airline;
    private LocalDate departureDate;
    private String affiliateUrl;
}

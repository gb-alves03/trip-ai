package com.trip_ai.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TravelPreference {
    private UUID id;
    private User user;
    private String originIata;
    private String destinationIata;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private boolean flexibleDates;
    private BigDecimal maxBudget;
    private boolean directOnly;
    @Builder.Default
    private int passengers = 1;
    private boolean active;
}

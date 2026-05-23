package com.trip_ai.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class FlightOffer {
    BigDecimal price;
    String airline;
    int stops;
    LocalDate departureDate;
    String affiliateUrl;
}

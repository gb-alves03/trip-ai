package com.trip_ai.domain.port.out;

import com.trip_ai.domain.model.PriceHistory;

import java.math.BigDecimal;

public interface PriceHistoryRepositoryPort {
    void save(PriceHistory history);
    BigDecimal getAvg60Days(String origin, String destination);
}

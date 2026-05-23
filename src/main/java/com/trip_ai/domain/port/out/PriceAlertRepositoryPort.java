package com.trip_ai.domain.port.out;

import com.trip_ai.domain.model.PriceAlert;

public interface PriceAlertRepositoryPort {
    void save(PriceAlert alert);
}

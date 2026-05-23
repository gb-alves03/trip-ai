package com.trip_ai.domain.port.out;

import com.trip_ai.domain.model.TravelPreference;

import java.util.List;
import java.util.UUID;

public interface TravelPreferenceRepositoryPort {
    List<TravelPreference> findAllActive();
    List<TravelPreference> findActiveByUserId(UUID userId);
    TravelPreference save(TravelPreference preference);
    void deactivateByUserId(UUID userId);
}

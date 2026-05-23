package com.trip_ai.infra.persistence.adapter;

import com.trip_ai.domain.model.PriceAlert;
import com.trip_ai.domain.port.out.PriceAlertRepositoryPort;
import com.trip_ai.infra.persistence.entity.PriceAlertEntity;
import com.trip_ai.infra.persistence.entity.TravelPreferenceEntity;
import com.trip_ai.infra.persistence.repository.PriceAlertJpaRepository;
import com.trip_ai.infra.persistence.repository.TravelPreferenceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceAlertPersistenceAdapter implements PriceAlertRepositoryPort {

    private final PriceAlertJpaRepository jpaRepo;
    private final TravelPreferenceJpaRepository prefJpaRepo;

    @Override
    public void save(PriceAlert alert) {
        TravelPreferenceEntity prefEntity = prefJpaRepo.findById(alert.getPreferenceId())
            .orElseThrow(() -> new IllegalStateException("Preference not found: " + alert.getPreferenceId()));

        jpaRepo.save(PriceAlertEntity.builder()
            .preference(prefEntity)
            .priceFound(alert.getPriceFound())
            .priceAvgReference(alert.getPriceAvgReference())
            .dropPercentage(alert.getDropPercentage())
            .airline(alert.getAirline())
            .departureDate(alert.getDepartureDate())
            .affiliateUrl(alert.getAffiliateUrl())
            .build());
    }
}

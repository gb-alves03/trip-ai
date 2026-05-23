package com.trip_ai.infra.persistence.adapter;

import com.trip_ai.domain.model.PriceHistory;
import com.trip_ai.domain.port.out.PriceHistoryRepositoryPort;
import com.trip_ai.infra.persistence.entity.PriceHistoryEntity;
import com.trip_ai.infra.persistence.repository.PriceHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class PriceHistoryPersistenceAdapter implements PriceHistoryRepositoryPort {

    private final PriceHistoryJpaRepository jpaRepo;

    @Override
    public void save(PriceHistory history) {
        jpaRepo.save(PriceHistoryEntity.builder()
            .origin(history.getOrigin())
            .destination(history.getDestination())
            .departureDate(history.getDepartureDate())
            .price(history.getPrice())
            .airline(history.getAirline())
            .stops(history.getStops())
            .affiliateUrl(history.getAffiliateUrl())
            .build());
    }

    @Override
    public BigDecimal getAvg60Days(String origin, String destination) {
        Instant since = Instant.now().minus(60, ChronoUnit.DAYS);
        return jpaRepo.getAvgPriceSince(origin, destination, since);
    }
}

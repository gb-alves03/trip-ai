package com.trip_ai.infra.persistence.repository;

import com.trip_ai.infra.persistence.entity.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface PriceHistoryJpaRepository extends JpaRepository<PriceHistoryEntity, UUID> {

    @Query("SELECT AVG(p.price) FROM PriceHistoryEntity p " +
           "WHERE p.origin = :origin AND p.destination = :destination AND p.capturedAt >= :since")
    BigDecimal getAvgPriceSince(
        @Param("origin") String origin,
        @Param("destination") String destination,
        @Param("since") Instant since);
}

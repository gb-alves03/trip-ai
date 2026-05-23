package com.trip_ai.infra.persistence.repository;

import com.trip_ai.infra.persistence.entity.TravelPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelPreferenceJpaRepository extends JpaRepository<TravelPreferenceEntity, UUID> {

    @Query("SELECT t FROM TravelPreferenceEntity t JOIN FETCH t.user WHERE t.active = true")
    List<TravelPreferenceEntity> findAllActiveWithUser();

    @Query("SELECT t FROM TravelPreferenceEntity t WHERE t.user.id = :userId AND t.active = true")
    List<TravelPreferenceEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE TravelPreferenceEntity t SET t.active = false WHERE t.user.id = :userId")
    void deactivateByUserId(@Param("userId") UUID userId);
}

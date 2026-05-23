package com.trip_ai.infra.persistence.adapter;

import com.trip_ai.domain.model.TravelPreference;
import com.trip_ai.domain.port.out.TravelPreferenceRepositoryPort;
import com.trip_ai.infra.persistence.entity.TravelPreferenceEntity;
import com.trip_ai.infra.persistence.entity.UserEntity;
import com.trip_ai.infra.persistence.repository.TravelPreferenceJpaRepository;
import com.trip_ai.infra.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TravelPreferencePersistenceAdapter implements TravelPreferenceRepositoryPort {

    private final TravelPreferenceJpaRepository jpaRepo;
    private final UserJpaRepository userJpaRepo;
    private final UserPersistenceAdapter userAdapter;

    @Override
    public List<TravelPreference> findAllActive() {
        return jpaRepo.findAllActiveWithUser().stream().map(this::toDomain).toList();
    }

    @Override
    public List<TravelPreference> findActiveByUserId(UUID userId) {
        return jpaRepo.findActiveByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public TravelPreference save(TravelPreference pref) {
        TravelPreferenceEntity entity = pref.getId() != null
            ? jpaRepo.findById(pref.getId()).orElseGet(TravelPreferenceEntity::new)
            : new TravelPreferenceEntity();

        UserEntity userEntity = userJpaRepo.findById(pref.getUser().getId())
            .orElseThrow(() -> new IllegalStateException("User not found: " + pref.getUser().getId()));
        entity.setUser(userEntity);
        entity.setOriginIata(pref.getOriginIata());
        entity.setDestinationIata(pref.getDestinationIata());
        entity.setDateFrom(pref.getDateFrom());
        entity.setDateTo(pref.getDateTo());
        entity.setFlexibleDates(pref.isFlexibleDates());
        entity.setMaxBudget(pref.getMaxBudget());
        entity.setDirectOnly(pref.isDirectOnly());
        entity.setPassengers(pref.getPassengers());
        entity.setActive(pref.isActive());
        return toDomain(jpaRepo.save(entity));
    }

    @Override
    @Transactional
    public void deactivateByUserId(UUID userId) {
        jpaRepo.deactivateByUserId(userId);
    }

    TravelPreference toDomain(TravelPreferenceEntity e) {
        return TravelPreference.builder()
            .id(e.getId())
            .user(userAdapter.toDomain(e.getUser()))
            .originIata(e.getOriginIata())
            .destinationIata(e.getDestinationIata())
            .dateFrom(e.getDateFrom())
            .dateTo(e.getDateTo())
            .flexibleDates(e.isFlexibleDates())
            .maxBudget(e.getMaxBudget())
            .directOnly(e.isDirectOnly())
            .passengers(e.getPassengers())
            .active(e.isActive())
            .build();
    }
}

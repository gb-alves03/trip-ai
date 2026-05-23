package com.trip_ai.infra.persistence.adapter;

import com.trip_ai.domain.model.ConversationFsm;
import com.trip_ai.domain.port.out.ConversationRepositoryPort;
import com.trip_ai.infra.persistence.entity.ConversationStateEntity;
import com.trip_ai.infra.persistence.repository.ConversationStateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConversationPersistenceAdapter implements ConversationRepositoryPort {

    private final ConversationStateJpaRepository jpaRepo;

    @Override
    public Optional<ConversationFsm> findByUserId(UUID userId) {
        return jpaRepo.findById(userId).map(this::toDomain);
    }

    @Override
    public ConversationFsm save(ConversationFsm fsm) {
        ConversationStateEntity entity = jpaRepo.findById(fsm.getUserId())
            .orElse(ConversationStateEntity.builder().userId(fsm.getUserId()).build());
        entity.setState(fsm.getState());
        entity.setContext(fsm.getContext());
        return toDomain(jpaRepo.save(entity));
    }

    private ConversationFsm toDomain(ConversationStateEntity e) {
        return ConversationFsm.builder()
            .userId(e.getUserId())
            .state(e.getState())
            .context(e.getContext())
            .build();
    }
}

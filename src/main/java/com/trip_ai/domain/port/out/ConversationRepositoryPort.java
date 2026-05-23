package com.trip_ai.domain.port.out;

import com.trip_ai.domain.model.ConversationFsm;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepositoryPort {
    Optional<ConversationFsm> findByUserId(UUID userId);
    ConversationFsm save(ConversationFsm fsm);
}

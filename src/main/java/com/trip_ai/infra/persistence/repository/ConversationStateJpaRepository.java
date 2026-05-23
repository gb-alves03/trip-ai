package com.trip_ai.infra.persistence.repository;

import com.trip_ai.infra.persistence.entity.ConversationStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationStateJpaRepository extends JpaRepository<ConversationStateEntity, UUID> {
}

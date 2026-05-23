package com.trip_ai.infra.persistence.entity;

import com.trip_ai.domain.enums.BotState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationStateEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private BotState state = BotState.IDLE;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String context = "{}";

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

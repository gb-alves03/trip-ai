package com.trip_ai.domain.model;

import com.trip_ai.domain.enums.BotState;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ConversationFsm {
    private UUID userId;
    @Builder.Default
    private BotState state = BotState.IDLE;
    @Builder.Default
    private String context = "{}";
}

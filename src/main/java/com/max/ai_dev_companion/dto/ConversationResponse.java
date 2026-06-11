package com.max.ai_dev_companion.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a conversation with its message history.
 */
public record ConversationResponse(
        UUID id,
        String title,
        Instant createdAt,
        List<MessageResponse> messages
) {
}

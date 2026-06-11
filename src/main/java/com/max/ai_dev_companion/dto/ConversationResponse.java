package com.max.ai_dev_companion.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String title,
        Instant createdAt,
        List<MessageResponse> messages
) {
}

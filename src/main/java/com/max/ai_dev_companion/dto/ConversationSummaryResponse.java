package com.max.ai_dev_companion.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID id,
        String title,
        Instant createdAt
) {
}

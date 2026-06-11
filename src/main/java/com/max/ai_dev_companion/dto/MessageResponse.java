package com.max.ai_dev_companion.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a conversation message.
 */
public record MessageResponse(
        UUID id,
        String role,
        String content,
        Instant createdAt
) {
}

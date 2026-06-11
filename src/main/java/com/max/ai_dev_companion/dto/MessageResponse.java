package com.max.ai_dev_companion.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        String role,
        String content,
        Instant createdAt
) {
}

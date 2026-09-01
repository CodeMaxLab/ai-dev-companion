package com.max.ai_dev_companion.dto;

import java.time.Instant;
import java.util.UUID;

import com.max.ai_dev_companion.model.MessageStatus;

/**
 * DTO representing a conversation message.
 */
public record MessageResponse(
        UUID id,
        String role,
        String content,
                Instant createdAt,
                MessageStatus status
) {
        public MessageResponse(UUID id, String role, String content, Instant createdAt) {
                this(id, role, content, createdAt, MessageStatus.COMPLETED);
        }
}

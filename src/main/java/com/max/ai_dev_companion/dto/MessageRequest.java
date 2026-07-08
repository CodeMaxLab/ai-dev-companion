package com.max.ai_dev_companion.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for sending a message within a conversation.
 *
 * @param content the content of the message to send
 * @param projectId optional project identifier used for RAG retrieval
 */
public record MessageRequest(
        @NotBlank(message = "Le message ne peut pas être vide")
        String content,
        UUID projectId
) {
}

package com.max.ai_dev_companion.dto;

import java.util.UUID;

/**
 * Request payload for a single chat message.
 *
 * @param message the user message to send to the model
 * @param projectId optional project identifier used for RAG retrieval
 */
public record ChatRequest(
        String message,
        UUID projectId
) {
}
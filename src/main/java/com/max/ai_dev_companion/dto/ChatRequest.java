package com.max.ai_dev_companion.dto;

/**
 * Request payload for a single chat message.
 *
 * @param message the user message to send to the model
 */
public record ChatRequest(
        String message
) {
}
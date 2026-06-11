package com.max.ai_dev_companion.dto;

/**
 * Response payload for a single chat completion.
 *
 * @param response the text returned by the AI model
 */
public record ChatResponse(
        String response
) {
}

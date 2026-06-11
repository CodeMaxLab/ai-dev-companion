package com.max.ai_dev_companion.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for sending a message within a conversation.
 *
 * @param content the content of the message to send
 */
public record MessageRequest(
        @NotBlank(message = "Le message ne peut pas être vide")
        String content
) {
}

package com.max.ai_dev_companion.dto;

/**
 * Request payload for creating a new conversation.
 *
 * @param title the title of the new conversation
 */
public record CreateConversationRequest(
        String title
) {
}

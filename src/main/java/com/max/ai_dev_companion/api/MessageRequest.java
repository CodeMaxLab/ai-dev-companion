package com.max.ai_dev_companion.api;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank(message = "Le message ne peut pas être vide")
        String content
) {
}

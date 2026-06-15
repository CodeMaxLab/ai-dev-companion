package com.max.ai_dev_companion.dto;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String rootPath
) {
}

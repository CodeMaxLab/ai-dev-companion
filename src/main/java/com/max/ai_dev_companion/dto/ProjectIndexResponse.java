package com.max.ai_dev_companion.dto;

import java.util.UUID;

public record ProjectIndexResponse(
        UUID projectId,
        int filesIndexed,
        int chunksGenerated,
        int embeddingsGenerated
) {
}

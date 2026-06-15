package com.max.ai_dev_companion.dto;

public record ProjectFileResponse(
        String relativePath,
        String fileName,
        long sizeBytes
) {
}

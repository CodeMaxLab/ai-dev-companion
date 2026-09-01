package com.max.ai_dev_companion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.max.ai_dev_companion.application.port.EmbeddingGateway;
import com.max.ai_dev_companion.model.Chunk;
import com.max.ai_dev_companion.repository.ChunkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectContextService {

    private final EmbeddingGateway embeddingGateway;
    private final ChunkRepository chunkRepository;

    @Value("${rag.top-k:5}")
    private int ragTopK;

    public String buildPrompt(String message, UUID projectId) {
        if (projectId == null || message == null || message.isBlank()) {
            return message;
        }

        try {
            float[] queryVector = embeddingGateway.embed(message.trim());
            String vectorLiteral = toPgVectorLiteral(queryVector);
            List<Chunk> nearestChunks = chunkRepository.findNearestByEmbeddingAndProjectId(
                    projectId,
                    vectorLiteral,
                    Math.max(1, ragTopK)
            );

            if (nearestChunks.isEmpty()) {
                return message;
            }

            return buildRagPrompt(message, nearestChunks);
        } catch (Exception ex) {
            log.warn("Retrieval failed for project {}, falling back to plain prompt", projectId, ex);
            return message;
        }
    }

    private String buildRagPrompt(String userQuestion, List<Chunk> chunks) {
        List<String> contextBlocks = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            contextBlocks.add("[" + (i + 1) + "] " + chunk.getSourcePath() + " (" + chunk.getStartOffset() + "-" + chunk.getEndOffset() + ")\n"
                    + chunk.getText());
        }

        String context = String.join("\n\n", contextBlocks);
        return """
                You are a coding assistant. Use only the provided project context when answering.
                If the context is insufficient, say what is missing instead of inventing details.

                Project context:
                %s

                User question:
                %s
                """.formatted(context, userQuestion);
    }

    private String toPgVectorLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }
}
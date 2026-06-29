package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.max.ai_dev_companion.infrastructure.llm.OllamaEmbeddingClient;
import com.max.ai_dev_companion.model.Chunk;
import com.max.ai_dev_companion.repository.ChunkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates and stores vector embeddings for project chunks.
 *
 * <p>This service loads chunks that do not yet have an embedding, asks a local
 * Ollama embedding model to generate one vector per chunk, and persists
 * vectors into PostgreSQL pgvector column.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkEmbeddingService {

    private static final int MAX_EMBEDDING_INPUT_CHARS = 1000;

    private final ChunkRepository chunkRepository;
    private final OllamaEmbeddingClient embeddingClient;

    /**
     * Generates embeddings for all chunks of a project that are still missing one.
     *
     * @param projectId the project identifier
     * @return number of chunks successfully embedded
     */
    @Transactional
    public int generateEmbeddingsForProject(UUID projectId) {
        List<Chunk> chunks = chunkRepository.findWithoutEmbeddingByProjectId(projectId);
        int generated = 0;

        for (Chunk chunk : chunks) {
            if (chunk.getText() == null || chunk.getText().isBlank()) {
                continue;
            }

            try {
                String embeddingInput = sanitizeEmbeddingInput(chunk);
                if (embeddingInput.isBlank()) {
                    continue;
                }
                float[] vector = embeddingClient.embed(embeddingInput);
                String vectorLiteral = toPgVectorLiteral(vector);
                int updatedRows = chunkRepository.updateEmbedding(chunk.getId(), vectorLiteral);
                if (updatedRows > 0) {
                    generated++;
                }
            } catch (Exception ex) {
                log.warn("Failed to generate embedding for chunk {}", chunk.getId(), ex);
            }
        }

        return generated;
    }

    private String sanitizeEmbeddingInput(Chunk chunk) {
        String text = chunk.getText().trim();
        if (text.length() <= MAX_EMBEDDING_INPUT_CHARS) {
            return text;
        }

        log.debug("Truncating chunk {} embedding input from {} to {} chars", chunk.getId(), text.length(), MAX_EMBEDDING_INPUT_CHARS);
        return text.substring(0, MAX_EMBEDDING_INPUT_CHARS);
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

package com.max.ai_dev_companion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.max.ai_dev_companion.infrastructure.llm.OllamaEmbeddingClient;
import com.max.ai_dev_companion.model.Chunk;
import com.max.ai_dev_companion.model.Message;
import com.max.ai_dev_companion.repository.ChunkRepository;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for sending prompts to the LLM.
 *
 * This service supports both one-shot chat calls and history-aware calls
 * where the conversation history is formatted and passed to the model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatModel model;
    private final OllamaEmbeddingClient embeddingClient;
    private final ChunkRepository chunkRepository;

    @Value("${rag.top-k:5}")
    private int ragTopK;

    /**
     * Send a single user message to the LLM without conversation history.
     *
     * @param message the user prompt to send to the model
     * @return the raw response text returned by the LLM
     */
    public String chat(String message) {
        return chat(message, null);
    }

    /**
     * Send a single user message to the LLM and optionally augment it with
     * retrieved project context.
     *
     * @param message the user prompt to send to the model
     * @param projectId optional project id used for retrieval
     * @return the raw response text returned by the LLM
     */
    public String chat(String message, UUID projectId) {
        String prompt = buildPromptWithRetrieval(message, projectId);
        log.debug("Sending single message to LLM (projectId={}): {}", projectId, message);
        String response = model.chat(prompt);
        log.debug("LLM response: {}", response);
        return response;
    }

    /**
     * Builds a prompt enriched with relevant chunks when a project id is provided.
     * Falls back to plain user message when retrieval cannot run.
     */
    public String buildPromptWithRetrieval(String message, UUID projectId) {
        if (projectId == null || message == null || message.isBlank()) {
            return message;
        }

        try {
            float[] queryVector = embeddingClient.embed(message.trim());
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

    /**
     * Send the full conversation history to the LLM as a single prompt.
     *
     * Each message is formatted with its role prefix ("user:" or "ai:") and
     * then joined with line breaks before being passed to the model.
     *
     * @param messages the ordered conversation history to send to the model
     * @return the raw response text returned by the LLM
     */
    public String chatWithHistory(List<Message> messages) {
        String prompt = buildPromptFromMessages(messages);
        log.debug("Sending conversation history to LLM ({} messages):\n{}", messages.size(), prompt);
        String response = model.chat(prompt);
        log.debug("LLM response: {}", response);
        return response;
    }

    /**
     * Format the conversation history for the LLM prompt.
     *
     * Each message is rendered as a single line prefixed by its role
     * ("user:" or "ai:") and joined with newline separators.
     *
     * @param messages the conversation messages to format
     * @return the prompt text ready to send to the model
     */
    private String buildPromptFromMessages(List<Message> messages) {
        return messages.stream()
                .map(m -> m.getRole().name().toLowerCase() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
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
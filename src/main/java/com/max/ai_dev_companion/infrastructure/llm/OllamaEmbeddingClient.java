package com.max.ai_dev_companion.infrastructure.llm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Small client around local Ollama embedding endpoint.
 *
 * <p>By default this client calls {@code http://localhost:11434/api/embeddings}
 * and expects a JSON payload containing an {@code embedding} array.</p>
 */
@Component
public class OllamaEmbeddingClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String model;

    public OllamaEmbeddingClient(
            @Value("${embedding.base-url:http://localhost:11434}") String baseUrl,
            @Value("${embedding.model:all-minilm}") String model) {
        this.restClient = RestClient.builder().build();
        this.baseUrl = baseUrl;
        this.model = model;
    }

    /**
     * Generates a sentence embedding for the provided text.
     *
     * @param text input text to vectorize
     * @return embedding vector
     */
    public float[] embed(String text) {
        JsonNode payload = restClient.post()
                .uri(normalizeUrl(baseUrl) + "/api/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            .body(new OllamaEmbeddingRequest(model, text))
                .retrieve()
            .body(JsonNode.class);

        return parseVector(payload);
    }

    private String normalizeUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private float[] parseVector(JsonNode payload) {
        if (payload == null || !payload.has("embedding") || !payload.get("embedding").isArray()) {
            throw new IllegalStateException("Invalid Ollama embedding response");
        }

        return toFloatArray(payload.get("embedding"));
    }

    private float[] toFloatArray(JsonNode arrayNode) {
        List<Float> values = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            values.add(node.floatValue());
        }

        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private record OllamaEmbeddingRequest(String model, String prompt) {
    }
}

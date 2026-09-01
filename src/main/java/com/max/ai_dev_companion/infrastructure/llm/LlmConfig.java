package com.max.ai_dev_companion.infrastructure.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class LlmConfig {

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-20b}")
    private String modelName;

    @PostConstruct
    public void init() {
        this.apiKey = normalizeApiKey(this.apiKey);
    }

    @Bean
    public ChatModel chatModel() {
        validateApiKey();
        log.debug("Using Groq API key: {}", maskApiKey(apiKey));
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        validateApiKey();
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Groq API key is missing. Set the GROQ_API_KEY environment variable or add it to the .env file.");
        }
    }

    private String maskApiKey(String key) {
        if (key == null || key.isBlank()) {
            return "<empty>";
        }
        if (key.length() <= 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 2);
    }

    private String normalizeApiKey(String key) {
        if (key == null) {
            return null;
        }

        String normalized = key.trim();
        if (normalized.length() >= 2 && ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'")))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }
}
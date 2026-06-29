package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.max.ai_dev_companion.model.Message;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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
    private final StreamingChatModel streamingModel;

    /**
     * Send a single user message to the LLM without conversation history.
     *
     * @param message the user prompt to send to the model
     * @return the raw response text returned by the LLM
     */
    public String chat(String message) {
        log.debug("Sending single message to LLM: {}", message);
        String response = model.chat(message);
        log.debug("LLM response: {}", response);
        return response;
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
     * Send a single user message to the streaming LLM endpoint.
     *
     * @param message the user prompt to stream to the model
     * @return a Flux of partial response tokens from the LLM
     */
    public Flux<String> stream(String message) {
        return Flux.<String>create(sink -> {
            try {
                streamingModel.chat(message, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        // Emit raw token; controller will wrap into SSE and set charset
                        sink.next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        sink.error(error);
                    }
                });
            } catch (Exception e) {
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
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
}
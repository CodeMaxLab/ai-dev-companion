package com.max.ai_dev_companion.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.max.ai_dev_companion.dto.ChatRequest;
import com.max.ai_dev_companion.dto.ChatResponse;
import com.max.ai_dev_companion.service.ChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * REST controller for chat operations.
 *
 * <p>Exposes endpoints for synchronous chat and streaming chat responses.
 */
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Sends a single chat message to the LLM and returns the full response.
     *
     * @param request the incoming chat request containing the user message
     * @return a response DTO with the AI answer
     */
    @PostMapping(
            value = "/chat",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ChatResponse chat(
            @RequestBody ChatRequest request
    ) {
        return new ChatResponse(
                chatService.chat(request.message())
        );
    }

    /**
     * Opens a server-sent event stream for partial LLM outputs.
     *
     * @param request the incoming chat request containing the user message
     * @return a flux of SSE events for each partial token emitted by the model
     */
    @PostMapping(
            value = "/chat/stream",
            produces = "text/event-stream;charset=UTF-8"
    )
    public Flux<ServerSentEvent<String>> stream(
            @RequestBody ChatRequest request
    ) {
        return chatService.stream(request.message())
                .map(token -> ServerSentEvent.builder(token).build());
    }
}
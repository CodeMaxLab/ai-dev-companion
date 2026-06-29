package com.max.ai_dev_companion.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.max.ai_dev_companion.dto.ChatRequest;
import com.max.ai_dev_companion.dto.ChatResponse;
import com.max.ai_dev_companion.service.ChatService;

import lombok.RequiredArgsConstructor;

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
         * @return an SSE emitter that pushes each partial token emitted by the model
     */
    @PostMapping(
            value = "/chat/stream",
            produces = "text/event-stream;charset=UTF-8"
    )
        public SseEmitter stream(
            @RequestBody ChatRequest request
    ) {
                SseEmitter emitter = new SseEmitter(0L);
                chatService.stream(
                                request.message(),
                                token -> {
                                        try {
                                                emitter.send(SseEmitter.event().data(token));
                                        } catch (Exception ex) {
                                                emitter.completeWithError(ex);
                                        }
                                },
                                emitter::complete,
                                emitter::completeWithError
                );
                return emitter;
    }
}
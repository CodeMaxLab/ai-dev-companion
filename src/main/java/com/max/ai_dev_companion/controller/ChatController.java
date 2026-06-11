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

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

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
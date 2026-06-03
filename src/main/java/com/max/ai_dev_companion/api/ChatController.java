package com.max.ai_dev_companion.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.max.ai_dev_companion.application.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    public record ChatRequest(String message) {}

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        System.out.println("MESSAGE = " + request);
        return chatService.chat(request.message());
    }
}
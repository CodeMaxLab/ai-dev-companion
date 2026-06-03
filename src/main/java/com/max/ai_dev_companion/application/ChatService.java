package com.max.ai_dev_companion.application;

import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel model;

    public String chat(String message) {
        return model.chat(message);
    }
}
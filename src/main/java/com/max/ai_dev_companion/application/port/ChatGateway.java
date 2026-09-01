package com.max.ai_dev_companion.application.port;

import java.util.List;
import java.util.UUID;

import com.max.ai_dev_companion.model.Message;

public interface ChatGateway {

    String chat(String message, UUID projectId);

    String chatWithHistory(List<Message> messages);
}
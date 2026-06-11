package com.max.ai_dev_companion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.max.ai_dev_companion.dto.ConversationResponse;
import com.max.ai_dev_companion.dto.ConversationSummaryResponse;
import com.max.ai_dev_companion.dto.CreateConversationRequest;
import com.max.ai_dev_companion.dto.MessageRequest;
import com.max.ai_dev_companion.dto.MessageResponse;
import com.max.ai_dev_companion.service.ConversationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversationResponse createConversation(@RequestBody @Valid CreateConversationRequest request) {
        return conversationService.createConversation(request.title());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConversationSummaryResponse> listConversations() {
        return conversationService.listConversations();
    }

    @GetMapping(value = "/{conversationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversationResponse getConversation(@PathVariable UUID conversationId) {
        return conversationService.getConversation(conversationId);
    }

    @GetMapping(value = "/{conversationId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MessageResponse> getConversationMessages(@PathVariable UUID conversationId) {
        return conversationService.getConversationMessages(conversationId);
    }

    @PostMapping(value = "/{conversationId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MessageResponse sendMessage(@PathVariable UUID conversationId,
                                       @RequestBody @Valid MessageRequest request) {
        return conversationService.sendMessage(conversationId, request.content());
    }
}

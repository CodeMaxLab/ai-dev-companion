package com.max.ai_dev_companion.application;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.api.ConversationResponse;
import com.max.ai_dev_companion.api.ConversationSummaryResponse;
import com.max.ai_dev_companion.api.MessageResponse;
import com.max.ai_dev_companion.domain.Conversation;
import com.max.ai_dev_companion.domain.Message;
import com.max.ai_dev_companion.domain.MessageRole;
import com.max.ai_dev_companion.repository.ConversationRepository;
import com.max.ai_dev_companion.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChatService chatService;

    public ConversationResponse createConversation(String title) {
        String safeTitle = title == null || title.isBlank() ? "New conversation" : title.trim();
        Conversation conversation = new Conversation(safeTitle);
        Conversation saved = conversationRepository.save(conversation);
        return toConversationResponse(saved);
    }

    @Transactional
    public MessageResponse sendMessage(UUID conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));

        Message userMessage = new Message(MessageRole.USER, content);
        conversation.addMessage(userMessage);
        messageRepository.save(userMessage);

        // Build history (including the just-saved user message) and send to LLM
        List<Message> history = conversation.getMessages();
        log.debug("Conversation {} - messages count before LLM call: {}", conversationId, history.size());
        String aiResponse = chatService.chatWithHistory(history);
        log.debug("Conversation {} - received AI response length={}", conversationId, aiResponse == null ? 0 : aiResponse.length());

        Message aiMessage = new Message(MessageRole.AI, aiResponse);
        conversation.addMessage(aiMessage);
        messageRepository.save(aiMessage);

        return toMessageResponse(aiMessage);
    }

    public ConversationResponse getConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));

        return toConversationResponse(conversation);
    }

    public List<MessageResponse> getConversationMessages(UUID conversationId) {
        return getConversation(conversationId).messages();
    }

    public List<ConversationSummaryResponse> listConversations() {
        return conversationRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        List<MessageResponse> messages = conversation.getMessages().stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                List.copyOf(messages)
        );
    }

    private ConversationSummaryResponse toSummaryResponse(Conversation conversation) {
        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}

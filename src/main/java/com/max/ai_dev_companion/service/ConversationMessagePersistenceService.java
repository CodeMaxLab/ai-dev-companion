package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.dto.MessageResponse;
import com.max.ai_dev_companion.model.Conversation;
import com.max.ai_dev_companion.model.Message;
import com.max.ai_dev_companion.model.MessageRole;
import com.max.ai_dev_companion.model.MessageStatus;
import com.max.ai_dev_companion.repository.ConversationRepository;
import com.max.ai_dev_companion.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationMessagePersistenceService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public PreparedConversationMessage saveUserMessage(UUID conversationId, String content) {
        Conversation conversation = findConversationWithMessages(conversationId);
        Message userMessage = new Message(MessageRole.USER, content, MessageStatus.COMPLETED);
        conversation.addMessage(userMessage);
        messageRepository.save(userMessage);

        return new PreparedConversationMessage(content, List.copyOf(conversation.getMessages()));
    }

    @Transactional
    public MessageResponse createPendingAiMessage(UUID conversationId) {
        Conversation conversation = findConversation(conversationId);
        Message aiMessage = new Message(MessageRole.AI, "", MessageStatus.PENDING);
        conversation.addMessage(aiMessage);
        messageRepository.save(aiMessage);

        return toMessageResponse(aiMessage);
    }

    @Transactional
    public MessageResponse completeAiMessage(UUID messageId, String content) {
        Message aiMessage = findMessage(messageId);
        aiMessage.markCompleted(content);
        messageRepository.save(aiMessage);
        return toMessageResponse(aiMessage);
    }

    @Transactional
    public MessageResponse failAiMessage(UUID messageId, String content) {
        Message aiMessage = findMessage(messageId);
        aiMessage.markFailed(content);
        messageRepository.save(aiMessage);
        return toMessageResponse(aiMessage);
    }

    private Conversation findConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));
    }

    private Conversation findConversationWithMessages(UUID conversationId) {
        return conversationRepository.findWithMessagesById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));
    }

    private Message findMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message non trouvé"));
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt(),
                message.getStatus()
        );
    }

    public record PreparedConversationMessage(String content, List<Message> history) {
    }
}
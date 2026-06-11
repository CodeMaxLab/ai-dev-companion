package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.dto.ConversationResponse;
import com.max.ai_dev_companion.dto.ConversationSummaryResponse;
import com.max.ai_dev_companion.dto.MessageResponse;
import com.max.ai_dev_companion.model.Conversation;
import com.max.ai_dev_companion.model.Message;
import com.max.ai_dev_companion.model.MessageRole;
import com.max.ai_dev_companion.repository.ConversationRepository;
import com.max.ai_dev_companion.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service managing the lifecycle of conversations.
 *
 * <p>This service is responsible for creating conversations, adding user
 * messages, calling the language model to obtain an AI response, and converting
 * entities into DTOs exposed by the controllers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChatService chatService;

    /**
     * Creates a new conversation with a safe title.
     *
     * @param title the title provided by the client; an empty or null value is replaced
     *              with the default title {@code "New conversation"}
     * @return the DTO representing the created conversation
     */
    public ConversationResponse createConversation(String title) {
        String safeTitle = title == null || title.isBlank() ? "New conversation" : title.trim();
        Conversation conversation = new Conversation(safeTitle);
        Conversation saved = conversationRepository.save(conversation);
        return toConversationResponse(saved);
    }

    /**
     * Adds a user message to an existing conversation, sends the history to the
     * language model, and stores the AI response in the conversation.
     *
     * @param conversationId the identifier of the targeted conversation
     * @param content        the content of the user message
     * @return the DTO of the generated AI message
     * @throws ResponseStatusException if the conversation does not exist
     */
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

    /**
     * Retrieves a complete conversation with its messages.
     *
     * @param conversationId the identifier of the requested conversation
     * @return the DTO representing the conversation
     * @throws ResponseStatusException if the conversation does not exist
     */
    public ConversationResponse getConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));

        return toConversationResponse(conversation);
    }

    /**
     * Returns only the messages of a conversation.
     *
     * @param conversationId the identifier of the requested conversation
     * @return the list of messages as DTOs
     */
    public List<MessageResponse> getConversationMessages(UUID conversationId) {
        return getConversation(conversationId).messages();
    }

    /**
     * Lists all existing conversations as summaries.
     *
     * @return the list of available conversations
     */
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

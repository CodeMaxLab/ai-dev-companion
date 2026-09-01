package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.application.port.ChatGateway;
import com.max.ai_dev_companion.dto.ConversationResponse;
import com.max.ai_dev_companion.dto.ConversationSummaryResponse;
import com.max.ai_dev_companion.dto.MessageResponse;
import com.max.ai_dev_companion.model.Conversation;
import com.max.ai_dev_companion.model.Message;
import com.max.ai_dev_companion.model.MessageRole;
import com.max.ai_dev_companion.repository.ConversationRepository;
import com.max.ai_dev_companion.repository.MessageRepository;


@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatGateway chatGateway;

    @Mock
    private ConversationMessagePersistenceService messagePersistenceService;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void createConversation_shouldSaveConversationWithTitle() {
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationResponse response = conversationService.createConversation("New title");

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.messages()).isEmpty();
        assertThat(response.id()).isNull();
    }

    @Test
    void createConversation_shouldUseDefaultTitleWhenBlank() {
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationResponse response = conversationService.createConversation("   ");

        assertThat(response.title()).isEqualTo("New conversation");
    }

    @Test
    void sendMessage_shouldSaveUserAndAiMessagesAndReturnAiResponse() {
        UUID conversationId = UUID.randomUUID();
        UUID pendingMessageId = UUID.randomUUID();
        Message userMessage = new Message(MessageRole.USER, "Hello");
        Message aiMessage = new Message(MessageRole.AI, "AI Response");
        when(messagePersistenceService.saveUserMessage(conversationId, "Hello"))
            .thenReturn(new ConversationMessagePersistenceService.PreparedConversationMessage(
                "Hello", List.of(userMessage)));
        when(messagePersistenceService.createPendingAiMessage(conversationId))
            .thenReturn(new MessageResponse(pendingMessageId, "AI", "", aiMessage.getCreatedAt(),
                    com.max.ai_dev_companion.model.MessageStatus.PENDING));
        when(messagePersistenceService.completeAiMessage(pendingMessageId, "AI Response"))
            .thenReturn(new MessageResponse(pendingMessageId, "AI", "AI Response", aiMessage.getCreatedAt()));
        when(chatGateway.chatWithHistory(any())).thenReturn("AI Response");

        MessageResponse response = conversationService.sendMessage(conversationId, "Hello");

        assertThat(response.role()).isEqualTo("AI");
        assertThat(response.content()).isEqualTo("AI Response");
        verify(messagePersistenceService).saveUserMessage(conversationId, "Hello");
        verify(messagePersistenceService).createPendingAiMessage(conversationId);
        verify(messagePersistenceService).completeAiMessage(pendingMessageId, "AI Response");
        }

        @Test
        void sendMessage_shouldMarkAiMessageAsFailedWhenGatewayFails() {
        UUID conversationId = UUID.randomUUID();
        UUID pendingMessageId = UUID.randomUUID();
        when(messagePersistenceService.saveUserMessage(conversationId, "Hello"))
            .thenReturn(new ConversationMessagePersistenceService.PreparedConversationMessage(
                "Hello", List.of(new Message(MessageRole.USER, "Hello"))));
        when(messagePersistenceService.createPendingAiMessage(conversationId))
            .thenReturn(new MessageResponse(pendingMessageId, "AI", "", java.time.Instant.now(),
                com.max.ai_dev_companion.model.MessageStatus.PENDING));
        when(chatGateway.chatWithHistory(any())).thenThrow(new IllegalStateException("LLM unavailable"));

        assertThatThrownBy(() -> conversationService.sendMessage(conversationId, "Hello"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("LLM unavailable");

        verify(messagePersistenceService).failAiMessage(pendingMessageId, "Unable to generate an AI response");
    }

    @Test
    void getConversation_shouldReturnConversationResponse() {
        Conversation conversation = new Conversation("Test");
        Message message = new Message(MessageRole.USER, "Hi");
        conversation.addMessage(message);
        when(conversationRepository.findById(any(UUID.class))).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getConversation(UUID.randomUUID());

        assertThat(response.title()).isEqualTo("Test");
        assertThat(response.messages()).hasSize(1);
        assertThat(response.messages().get(0).content()).isEqualTo("Hi");
    }

    @Test
    void getConversationMessages_shouldReturnMessageResponses() {
        Conversation conversation = new Conversation("Test");
        Message message = new Message(MessageRole.USER, "Bonjour");
        conversation.addMessage(message);
        when(conversationRepository.findWithMessagesById(any(UUID.class))).thenReturn(Optional.of(conversation));

        List<MessageResponse> messages = conversationService.getConversationMessages(UUID.randomUUID());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).content()).isEqualTo("Bonjour");
    }

    @Test
    void listConversations_shouldReturnSummaryResponses() {
        Conversation conversation = new Conversation("Discussion");
        when(conversationRepository.findAll()).thenReturn(List.of(conversation));

        List<ConversationSummaryResponse> summaries = conversationService.listConversations();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).title()).isEqualTo("Discussion");
    }

    @Test
    void getConversation_shouldThrowNotFoundWhenMissing() {
        when(conversationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversation(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Conversation non trouvée");
    }
}

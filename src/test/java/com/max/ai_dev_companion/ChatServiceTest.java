package com.max.ai_dev_companion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.max.ai_dev_companion.application.ChatService;
import com.max.ai_dev_companion.domain.Message;
import com.max.ai_dev_companion.domain.MessageRole;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatModel model;

    @Mock
    private StreamingChatModel streamingModel;

    @InjectMocks
    private ChatService chatService;

    @Test
    void chat_shouldDelegateToModel() {
        when(model.chat("Bonjour")).thenReturn("Salut");

        String response = chatService.chat("Bonjour");

        assertEquals("Salut", response);
    }

    @Test
    void chatWithHistory_shouldBuildPromptFromMessages() {
        Message user = new Message(MessageRole.USER, "Salut");
        Message ai = new Message(MessageRole.AI, "Bonjour");

        when(model.chat("user: Salut\nai: Bonjour")).thenReturn("Réponse");

        String response = chatService.chatWithHistory(List.of(user, ai));

        assertEquals("Réponse", response);
    }

    @Test
    void stream_shouldEmitTokensAndComplete() {
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("token1");
            handler.onPartialResponse("token2");
            handler.onCompleteResponse(null);
            return null;
        }).when(streamingModel).chat(anyString(), any(StreamingChatResponseHandler.class));

        StepVerifier.create(chatService.stream("Bonjour"))
                .expectNext("token1", "token2")
                .verifyComplete();
    }
}

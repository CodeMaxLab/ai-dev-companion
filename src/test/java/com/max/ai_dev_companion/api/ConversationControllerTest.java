package com.max.ai_dev_companion.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.max.ai_dev_companion.application.ConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ConversationService conversationService;

    @Test
    void createConversation_shouldReturnConversationResponse() {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.createConversation(eq("Sujet")))
                .thenReturn(new ConversationResponse(conversationId, "Sujet", Instant.now(), List.of()));

        webTestClient.post()
                .uri("/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateConversationRequest("Sujet"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Sujet")
                .jsonPath("$.id").isEqualTo(conversationId.toString());
    }

    @Test
    void listConversations_shouldReturnSummaries() {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.listConversations())
                .thenReturn(List.of(new ConversationSummaryResponse(conversationId, "Discussion", Instant.now())));

        webTestClient.get()
                .uri("/conversations")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Discussion")
                .jsonPath("$[0].id").isEqualTo(conversationId.toString());
    }

    @Test
    void sendMessage_shouldReturnMessageResponse() {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.sendMessage(eq(conversationId), eq("Hello")))
                .thenReturn(new MessageResponse(UUID.randomUUID(), "USER", "Hello", Instant.now()));

        webTestClient.post()
                .uri("/conversations/{conversationId}/messages", conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new MessageRequest("Hello"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isEqualTo("Hello")
                .jsonPath("$.role").isEqualTo("USER");
    }
}

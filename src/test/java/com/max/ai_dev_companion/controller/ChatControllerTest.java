package com.max.ai_dev_companion.controller;

import com.max.ai_dev_companion.dto.ChatRequest;
import com.max.ai_dev_companion.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatService chatService;

    @Test
    void chatEndpoint_shouldReturnChatResponse() {
        when(chatService.chat(eq("hello"))).thenReturn("world");

        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatRequest("hello"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo("world");
    }

    @Test
    void streamEndpoint_shouldReturnServerSentEvents() {
        when(chatService.stream(eq("hello"))).thenReturn(Flux.just("token1", "token2"));

        var result = webTestClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatRequest("hello"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class);

        StepVerifier.create(result.getResponseBody())
                .expectNextMatches(value -> value.contains("token1"))
                .expectNextMatches(value -> value.contains("token2"))
                .verifyComplete();
    }
}

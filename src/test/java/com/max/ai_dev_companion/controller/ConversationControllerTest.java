package com.max.ai_dev_companion.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.max.ai_dev_companion.dto.ConversationResponse;
import com.max.ai_dev_companion.dto.ConversationSummaryResponse;
import com.max.ai_dev_companion.dto.CreateConversationRequest;
import com.max.ai_dev_companion.dto.MessageRequest;
import com.max.ai_dev_companion.dto.MessageResponse;
import com.max.ai_dev_companion.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
        private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;

    @Test
    void createConversation_shouldReturnConversationResponse() throws Exception {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.createConversation(eq("Sujet")))
                .thenReturn(new ConversationResponse(conversationId, "Sujet", Instant.now(), List.of()));

        mockMvc.perform(post("/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sujet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sujet"))
                .andExpect(jsonPath("$.id").value(conversationId.toString()));
    }

    @Test
    void listConversations_shouldReturnSummaries() throws Exception {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.listConversations())
                .thenReturn(List.of(new ConversationSummaryResponse(conversationId, "Discussion", Instant.now())));

        mockMvc.perform(get("/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Discussion"))
                .andExpect(jsonPath("$[0].id").value(conversationId.toString()));
    }

    @Test
    void sendMessage_shouldReturnMessageResponse() throws Exception {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.sendMessage(eq(conversationId), eq("Hello")))
                .thenReturn(new MessageResponse(UUID.randomUUID(), "USER", "Hello", Instant.now()));

        mockMvc.perform(post("/conversations/{conversationId}/messages", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}

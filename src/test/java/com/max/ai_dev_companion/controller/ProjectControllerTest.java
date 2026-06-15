package com.max.ai_dev_companion.controller;

import java.util.List;
import java.util.UUID;

import com.max.ai_dev_companion.dto.CreateProjectRequest;
import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.dto.ProjectFileResponse;
import com.max.ai_dev_companion.dto.ProjectResponse;
import com.max.ai_dev_companion.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProjectService projectService;

    @Test
    void createProject_shouldReturnProjectResponse() {
        UUID projectId = UUID.randomUUID();
        when(projectService.createProject(eq("Demo"), eq("/tmp/demo")))
                .thenReturn(new ProjectResponse(projectId, "Demo", "/tmp/demo"));

        webTestClient.post()
                .uri("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateProjectRequest("Demo", "/tmp/demo"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.name").isEqualTo("Demo")
                .jsonPath("$.rootPath").isEqualTo("/tmp/demo");
    }

    @Test
    void getProjectFiles_shouldReturnInterestingFiles() {
        UUID projectId = UUID.randomUUID();
        when(projectService.listProjectFiles(eq(projectId)))
                .thenReturn(List.of(
                        new ProjectFileResponse("README.md", "README.md", 10),
                        new ProjectFileResponse("src/Main.java", "Main.java", 50)
                ));

        webTestClient.get()
                .uri("/projects/{projectId}/files", projectId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].relativePath").isEqualTo("README.md")
                .jsonPath("$[1].relativePath").isEqualTo("src/Main.java");
    }

    @Test
    void indexProjectFiles_shouldReturnIndexedFileCount() {
        UUID projectId = UUID.randomUUID();
        when(projectService.indexProjectFiles(eq(projectId)))
                .thenReturn(new IndexedFileCountResponse(2));

        webTestClient.post()
                .uri("/projects/{projectId}/files", projectId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.indexedFiles").isEqualTo(2);
    }
}

package com.max.ai_dev_companion.controller;

import java.util.List;
import java.util.UUID;

import com.max.ai_dev_companion.dto.CreateProjectRequest;
import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.dto.ProjectFileResponse;
import com.max.ai_dev_companion.dto.ProjectIndexResponse;
import com.max.ai_dev_companion.dto.ProjectResponse;
import com.max.ai_dev_companion.service.ProjectService;
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
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
        private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private com.max.ai_dev_companion.service.ProjectIndexService projectIndexService;

    @Test
    void createProject_shouldReturnProjectResponse() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(projectService.createProject(eq("Demo"), eq("/tmp/demo")))
                .thenReturn(new ProjectResponse(projectId, "Demo", "/tmp/demo"));

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Demo\",\"rootPath\":\"/tmp/demo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Demo"))
                .andExpect(jsonPath("$.rootPath").value("/tmp/demo"));
    }

    @Test
    void getProjectFiles_shouldReturnInterestingFiles() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(projectService.listProjectFiles(eq(projectId)))
                .thenReturn(List.of(
                        new ProjectFileResponse("README.md", "README.md", 10),
                        new ProjectFileResponse("src/Main.java", "Main.java", 50)
                ));

        mockMvc.perform(get("/projects/{projectId}/files", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].relativePath").value("README.md"))
                .andExpect(jsonPath("$[1].relativePath").value("src/Main.java"));
    }

    @Test
    void indexProjectFiles_shouldReturnIndexedFileCount() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(projectService.indexProjectFiles(eq(projectId)))
                .thenReturn(new IndexedFileCountResponse(2));

        mockMvc.perform(post("/projects/{projectId}/files", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexedFiles").value(2));
    }

        @Test
        void indexProject_shouldReturnFilesChunksAndEmbeddingsCounts() throws Exception {
                UUID projectId = UUID.randomUUID();
                when(projectIndexService.indexProject(eq(projectId)))
                                .thenReturn(new ProjectIndexResponse(projectId, 2, 6, 6));

                mockMvc.perform(post("/projects/{projectId}/index", projectId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                                .andExpect(jsonPath("$.filesIndexed").value(2))
                                .andExpect(jsonPath("$.chunksGenerated").value(6))
                                .andExpect(jsonPath("$.embeddingsGenerated").value(6));
        }
}

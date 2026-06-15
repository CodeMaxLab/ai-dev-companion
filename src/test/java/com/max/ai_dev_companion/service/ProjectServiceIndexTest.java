package com.max.ai_dev_companion.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.model.Project;
import com.max.ai_dev_companion.repository.CodeFileRepository;
import com.max.ai_dev_companion.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceIndexTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CodeFileRepository codeFileRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void indexProjectFiles_shouldDeleteExistingAndSaveNewFiles(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("README.md"));
        Files.createFile(tempDir.resolve("Example.java"));
        Path binary = Files.createFile(tempDir.resolve("image.png"));
        Files.writeString(binary, "binary");

        Project project = new Project("Demo", tempDir.toString());
        UUID projectId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(projectRepository.findById(eq(projectId))).thenReturn(Optional.of(project));

        IndexedFileCountResponse response = projectService.indexProjectFiles(projectId);

        assertThat(response.indexedFiles()).isEqualTo(2);
        verify(codeFileRepository).deleteByProjectId(projectId);
        verify(codeFileRepository).saveAll(any());
    }

    @Test
    void indexProjectFiles_shouldThrowNotFoundWhenProjectMissing() {
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.indexProjectFiles(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Projet non trouvé");
    }
}

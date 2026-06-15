package com.max.ai_dev_companion.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.dto.ProjectFileResponse;
import com.max.ai_dev_companion.dto.ProjectResponse;
import com.max.ai_dev_companion.model.Project;
import com.max.ai_dev_companion.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_shouldSaveProjectAndReturnResponse() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.createProject("Demo project", "C:/tmp/demo");

        assertThat(response.name()).isEqualTo("Demo project");
        assertThat(response.rootPath()).isEqualTo("C:/tmp/demo");
        assertThat(response.id()).isNull();
    }

    @Test
    void listProjectFiles_shouldReturnInterestingFiles(@TempDir Path tempDir) throws Exception {
        Path markdown = Files.createFile(tempDir.resolve("README.md"));
        Path javaFile = Files.createFile(tempDir.resolve("Example.java"));
        Path binaryFile = Files.createFile(tempDir.resolve("image.png"));

        Project project = new Project("Demo", tempDir.toString());
        when(projectRepository.findById(eq(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .thenReturn(Optional.of(project));

        List<ProjectFileResponse> files = projectService.listProjectFiles(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(files).hasSize(2);
        assertThat(files).extracting(ProjectFileResponse::fileName).containsExactlyInAnyOrder("README.md", "Example.java");
        assertThat(files).extracting(ProjectFileResponse::relativePath).containsExactlyInAnyOrder("README.md", "Example.java");
        assertThat(files).extracting(ProjectFileResponse::sizeBytes).containsExactlyInAnyOrder(0L, 0L);
    }

    @Test
    void listProjectFiles_shouldThrowNotFoundWhenProjectMissing() {
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.listProjectFiles(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Projet non trouvé");
    }

    @Test
    void listProjectFiles_shouldThrowBadRequestWhenRootPathInvalid() {
        Project project = new Project("Demo", "C:/does/not/exist");
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.listProjectFiles(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("répertoire valide");
    }
}

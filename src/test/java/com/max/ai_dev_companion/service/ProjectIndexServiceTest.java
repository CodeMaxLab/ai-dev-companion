package com.max.ai_dev_companion.service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.dto.ProjectIndexResponse;

@ExtendWith(MockitoExtension.class)
class ProjectIndexServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ChunkIndexingService chunkIndexingService;

    @Mock
    private ChunkEmbeddingService chunkEmbeddingService;

    @InjectMocks
    private ProjectIndexService service;

    @Test
    void indexProject_shouldOrchestrateFilesChunksAndEmbeddings() {
        UUID projectId = UUID.randomUUID();

        when(projectService.indexProjectFiles(projectId)).thenReturn(new IndexedFileCountResponse(4));
        when(chunkIndexingService.indexChunksForProject(projectId)).thenReturn(9);
        when(chunkEmbeddingService.generateEmbeddingsForProject(projectId)).thenReturn(7);

        ProjectIndexResponse response = service.indexProject(projectId);

        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.filesIndexed()).isEqualTo(4);
        assertThat(response.chunksGenerated()).isEqualTo(9);
        assertThat(response.embeddingsGenerated()).isEqualTo(7);

        verify(projectService).indexProjectFiles(projectId);
        verify(chunkIndexingService).indexChunksForProject(projectId);
        verify(chunkEmbeddingService).generateEmbeddingsForProject(projectId);
    }
}

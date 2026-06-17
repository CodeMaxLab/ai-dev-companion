package com.max.ai_dev_companion.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.dto.ProjectIndexResponse;
import com.max.ai_dev_companion.service.ChunkIndexingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectIndexService {

    /**
     * Orchestrateur de l'opération "indexer un projet" : délègue l'indexation
     * des fichiers à {@link com.max.ai_dev_companion.service.ProjectService} puis
     * déclenche la génération des chunks via {@link ChunkIndexingService}.
     */

    private final ProjectService projectService;
    private final ChunkIndexingService chunkIndexingService;

    @Transactional
    public ProjectIndexResponse indexProject(UUID projectId) {
        IndexedFileCountResponse indexedFiles = projectService.indexProjectFiles(projectId);
        int chunksGenerated = chunkIndexingService.indexChunksForProject(projectId);
        return new ProjectIndexResponse(projectId, indexedFiles.indexedFiles(), chunksGenerated);
    }
}

package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.max.ai_dev_companion.model.CodeFile;
import com.max.ai_dev_companion.repository.CodeFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChunkIndexingService {

    /**
     * Parcourt tous les `CodeFile` d'un projet et appelle le {@link ChunkingService}
     * pour générer et persister les chunks. Retourne le nombre total de chunks créés.
     */

    private final CodeFileRepository codeFileRepository;
    private final ChunkingService chunkingService;

    @Transactional
    public int indexChunksForProject(UUID projectId) {
        List<CodeFile> files = codeFileRepository.findByProjectId(projectId);
        return files.stream()
                .mapToInt(file -> chunkingService.chunkAndSave(file).size())
                .sum();
    }
}

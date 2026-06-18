package com.max.ai_dev_companion.service;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.max.ai_dev_companion.model.Chunk;
import com.max.ai_dev_companion.model.CodeFile;
import com.max.ai_dev_companion.repository.CodeFileRepository;

@ExtendWith(MockitoExtension.class)
class ChunkIndexingServiceTest {

    @Mock
    private CodeFileRepository codeFileRepository;

    @Mock
    private ChunkingService chunkingService;

    @InjectMocks
    private ChunkIndexingService service;

    @Test
    void indexChunksForProject_returnsSumOfChunks() {
        UUID projectId = UUID.randomUUID();
        CodeFile f1 = new CodeFile("a.txt", "one two three", null);
        CodeFile f2 = new CodeFile("b.txt", "alpha beta gamma", null);

        when(codeFileRepository.findByProjectId(projectId)).thenReturn(List.of(f1, f2));
        when(chunkingService.chunkAndSave(f1)).thenReturn(List.of(new Chunk(null, "a.txt", "t", 0, 1, null, null)));
        when(chunkingService.chunkAndSave(f2)).thenReturn(List.of(new Chunk(null, "b.txt", "t1", 0, 1, null, null), new Chunk(null, "b.txt", "t2", 1, 2, null, null)));

        int total = service.indexChunksForProject(projectId);

        assertEquals(3, total);
    }
}

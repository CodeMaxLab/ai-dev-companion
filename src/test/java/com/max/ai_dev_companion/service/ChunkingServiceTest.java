package com.max.ai_dev_companion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.max.ai_dev_companion.model.Chunk;
import com.max.ai_dev_companion.model.CodeFile;
import com.max.ai_dev_companion.repository.ChunkRepository;

@ExtendWith(MockitoExtension.class)
class ChunkingServiceTest {

    @Mock
    private ChunkRepository chunkRepository;

    @Test
    void chunkSmallContent_createsSingleChunk() {
        ChunkingService service = new ChunkingService();
        ReflectionTestUtils.setField(service, "chunkRepository", chunkRepository);

        CodeFile file = new CodeFile("path/to/file.java", "short content", null);

        when(chunkRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Chunk> chunks = service.chunkAndSave(file);

        assertEquals(1, chunks.size());
        verify(chunkRepository).saveAll(chunks);
    }

    @Test
    void chunkLargeContent_createsMultipleChunks() {
        ChunkingService service = new ChunkingService();
        ReflectionTestUtils.setField(service, "chunkRepository", chunkRepository);

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            b.append("word ");
        }
        CodeFile file = new CodeFile("big.txt", b.toString(), null);

        when(chunkRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Chunk> chunks = service.chunkAndSave(file);

        // Expect more than one chunk for this large content
        assertEquals(true, chunks.size() > 1);
        verify(chunkRepository).saveAll(chunks);
    }
}

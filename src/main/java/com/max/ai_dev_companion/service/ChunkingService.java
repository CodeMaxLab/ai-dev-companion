package com.max.ai_dev_companion.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.max.ai_dev_companion.model.Chunk;
import com.max.ai_dev_companion.model.CodeFile;
import com.max.ai_dev_companion.repository.ChunkRepository;

@Service
public class ChunkingService {

    /**
     * Découpe le contenu d'un `CodeFile` en plusieurs {@link com.max.ai_dev_companion.model.Chunk}.
     *
     * <p>La stratégie actuelle est basée sur une fenêtre en caractères avec recouvrement
     * pour préserver le contexte entre les chunks. Les chunks produits sont persistés
     * via le {@link com.max.ai_dev_companion.repository.ChunkRepository}.</p>
     */

    @Autowired
    private ChunkRepository chunkRepository;

    // Basic character-based chunking with overlap. Tunable parameters.
    private static final int DEFAULT_MAX_CHARS = 2000;
    private static final int DEFAULT_OVERLAP = 400;

    public List<Chunk> chunkAndSave(CodeFile file) {
        if (file == null || file.getContent() == null) {
            return List.of();
        }

        String content = file.getContent();
        int maxChars = DEFAULT_MAX_CHARS;
        int overlap = DEFAULT_OVERLAP;

        List<Chunk> chunks = new ArrayList<>();

        int start = 0;
        int length = content.length();

        while (start < length) {
            int end = Math.min(length, start + maxChars);

            // try to backtrack to nearest newline/space to avoid cutting words
            int split = end;
            for (int i = end; i > start + maxChars / 2 && i > 0; i--) {
                char c = content.charAt(i - 1);
                if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
                    split = i;
                    break;
                }
            }
            if (split == start) {
                split = end; // fallback
            }

            String chunkText = content.substring(start, split).trim();
            if (!chunkText.isEmpty()) {
                Chunk chunk = new Chunk(file.getId(), file.getPath(), chunkText, start, split, null);
                chunks.add(chunk);
            }

            if (split >= length) {
                break;
            }

            start = Math.max(0, split - overlap);
        }

        if (!chunks.isEmpty()) {
            chunkRepository.saveAll(chunks);
        }

        return chunks;
    }
}

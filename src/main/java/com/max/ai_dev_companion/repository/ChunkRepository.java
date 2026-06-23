package com.max.ai_dev_companion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.max.ai_dev_companion.model.Chunk;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

        @Query(value = """
                        SELECT c.*
                        FROM chunks c
                        JOIN code_files cf ON cf.id = c.source_id
                        WHERE cf.project_id = :projectId
                            AND c.embedding IS NULL
                        ORDER BY c.created_at ASC
                        """, nativeQuery = true)
        List<Chunk> findWithoutEmbeddingByProjectId(@Param("projectId") UUID projectId);

        @Modifying
        @Query(value = "UPDATE chunks SET embedding = CAST(:vector AS vector) WHERE id = :chunkId", nativeQuery = true)
        int updateEmbedding(@Param("chunkId") UUID chunkId, @Param("vector") String vector);

    @Query(value = "SELECT * FROM chunks WHERE embedding IS NOT NULL ORDER BY embedding <-> CAST(:vector AS vector) LIMIT :k", nativeQuery = true)
    List<Chunk> findNearestByEmbedding(@Param("vector") String vector, @Param("k") int k);
}

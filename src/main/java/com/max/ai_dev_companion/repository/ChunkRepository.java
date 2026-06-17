package com.max.ai_dev_companion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.max.ai_dev_companion.model.Chunk;
import com.pgvector.PGvector;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    @Query(value = "SELECT * FROM chunks ORDER BY embedding <-> :vector LIMIT :k", nativeQuery = true)
    List<Chunk> findNearestByEmbedding(@Param("vector") PGvector vector, @Param("k") int k);
}

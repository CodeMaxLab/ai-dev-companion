package com.max.ai_dev_companion.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chunks")
public class Chunk {

    /**
     * Représente un segment (chunk) extrait d'un fichier source.
     *
     * <p>Le chunk contient le texte extrait ainsi que les offsets dans le
     * document source afin de pouvoir retrouver et surligner la portion
     * correspondante ultérieurement.</p>
     */

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "source_path", nullable = false)
    private String sourcePath;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "start_offset", nullable = false)
    private int startOffset;

    @Column(name = "end_offset", nullable = false)
    private int endOffset;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(columnDefinition = "TEXT")
    private String embedding;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Chunk() {
    }

    public Chunk(UUID sourceId, String sourcePath, String text, int startOffset, int endOffset, String metadata,
            String embedding) {
        this.sourceId = sourceId;
        this.sourcePath = sourcePath;
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.metadata = metadata;
        this.embedding = embedding;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getText() {
        return text;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

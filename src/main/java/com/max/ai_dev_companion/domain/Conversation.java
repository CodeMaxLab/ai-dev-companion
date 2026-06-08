package com.max.ai_dev_companion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY,
            cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private final List<Message> messages = new ArrayList<>();

    protected Conversation() {
    }

    public Conversation(String title) {
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addMessage(Message message) {
        message.setConversation(this);
        this.messages.add(message);
    }
}

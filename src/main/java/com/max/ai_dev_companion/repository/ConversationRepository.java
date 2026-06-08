package com.max.ai_dev_companion.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.max.ai_dev_companion.domain.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}

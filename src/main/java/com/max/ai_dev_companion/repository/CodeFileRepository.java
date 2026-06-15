package com.max.ai_dev_companion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.max.ai_dev_companion.model.CodeFile;

@Repository
public interface CodeFileRepository extends JpaRepository<CodeFile, UUID> {

    List<CodeFile> findByProjectId(UUID projectId);

    void deleteByProjectId(UUID projectId);
}

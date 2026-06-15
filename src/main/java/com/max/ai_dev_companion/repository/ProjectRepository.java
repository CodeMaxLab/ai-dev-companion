package com.max.ai_dev_companion.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.max.ai_dev_companion.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
}

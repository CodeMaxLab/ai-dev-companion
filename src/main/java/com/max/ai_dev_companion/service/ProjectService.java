package com.max.ai_dev_companion.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.dto.ProjectFileResponse;
import com.max.ai_dev_companion.dto.ProjectResponse;
import com.max.ai_dev_companion.model.CodeFile;
import com.max.ai_dev_companion.model.Project;
import com.max.ai_dev_companion.repository.CodeFileRepository;
import com.max.ai_dev_companion.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CodeFileRepository codeFileRepository;

    /**
     * Creates a new project and persists it in the database.
     *
     * @param name the name of the project
     * @param rootPath the absolute path of the project root directory
     * @return a DTO representing the persisted project
     */
    @Transactional
    public ProjectResponse createProject(String name, String rootPath) {
        Project project = new Project(name.trim(), rootPath.trim());
        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    /**
     * Lists the interesting files for a project.
     *
     * <p>The method reads the configured project root directory and returns files
     * that are considered relevant for retrieval augmentation tasks.
     *
     * @param projectId the identifier of the project
     * @return the list of interesting project files
     */
    public List<ProjectFileResponse> listProjectFiles(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

        Path root = Path.of(project.getRootPath());
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le chemin du projet n'est pas un répertoire valide");
        }

        List<ProjectFileResponse> files = new ArrayList<>();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isInterestingFile(file, root)) {
                        files.add(new ProjectFileResponse(
                                root.relativize(file).toString().replace('\\', '/'),
                                file.getFileName().toString(),
                                attrs.size()
                        ));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Erreur lors de l'analyse des fichiers du projet {}", projectId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lister les fichiers du projet");
        }

        return files;
    }

    /**
     * Indexes the interesting files for a project in the database.
     *
     * <p>Existing saved code files for the project are removed before the current
     * interesting files are persisted.
     *
     * @param projectId the identifier of the project
     * @return the count of files indexed in this operation
     */
    @Transactional
    public IndexedFileCountResponse indexProjectFiles(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

        Path root = Path.of(project.getRootPath());
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le chemin du projet n'est pas un répertoire valide");
        }

        List<CodeFile> codeFiles = new ArrayList<>();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isInterestingFile(file, root)) {
                        try {
                            String relativePath = root.relativize(file).toString().replace('\\', '/');
                            String content = Files.readString(file);
                            codeFiles.add(new CodeFile(relativePath, content, project));
                        } catch (IOException ex) {
                            log.warn("Impossible de lire le fichier {} pour le projet {}", file, projectId, ex);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Erreur lors de l'analyse des fichiers du projet {}", projectId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lister les fichiers du projet");
        }

        codeFileRepository.deleteByProjectId(projectId);
        if (!codeFiles.isEmpty()) {
            codeFileRepository.saveAll(codeFiles);
        }

        return new IndexedFileCountResponse(codeFiles.size());
    }

    private boolean isInterestingFile(Path file, Path root) {
        String relativePath = root.relativize(file).toString().replace('\\', '/');
        String fileName = file.getFileName().toString().toLowerCase();

        if (relativePath.startsWith("frontend/node_modules") || relativePath.startsWith("tests") || relativePath.startsWith(".github")) {
            return false;
        }

        boolean interestingExtension =
                fileName.endsWith(".md")
                || fileName.endsWith(".java")
                || fileName.endsWith(".sql")
                || fileName.endsWith(".yml")
                || fileName.endsWith(".yaml")
                || fileName.endsWith(".txt")
                || fileName.endsWith(".properties")
                || fileName.endsWith(".py");

        return interestingExtension;
    }

    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getRootPath());
    }
}

package com.univer.bookcom.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogGenerationService {

    public enum Status { IN_PROGRESS, DONE, FAILED }

    private final Map<String, Status> statusMap = new ConcurrentHashMap<>();
    private final Map<String, Path> fileMap = new ConcurrentHashMap<>();
    private final Path baseDir = Paths.get("generated-logs");

    @PostConstruct
    public void init() throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
    }

    public String requestLogGeneration() {
        String id = UUID.randomUUID().toString();
        statusMap.put(id, Status.IN_PROGRESS);
        generateLogAsync(id);
        return id;
    }

    @Async
    public void generateLogAsync(String id) {
        try {
            Path source = Paths.get("application.log");
            if (!Files.exists(source)) {
                statusMap.put(id, Status.FAILED);
                return;
            }

            Path output = baseDir.resolve("log-" + id + ".log");
            Files.copy(source, output, StandardCopyOption.REPLACE_EXISTING);
            fileMap.put(id, output);
            statusMap.put(id, Status.DONE);
        } catch (IOException e) {
            statusMap.put(id, Status.FAILED);
        }
    }

    public Status getStatus(String id) {
        return statusMap.getOrDefault(id, null);
    }

    public Path getFile(String id) {
        return fileMap.get(id);
    }
}
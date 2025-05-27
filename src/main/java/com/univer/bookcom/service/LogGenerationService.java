package com.univer.bookcom.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LogGenerationService {

    public enum Status { IN_PROGRESS, DONE, FAILED }

    private final Map<String, Status> statusMap = new ConcurrentHashMap<>();
    private final Map<String, Path> filePaths = new ConcurrentHashMap<>();
    private final Path logsDir = Paths.get("logs");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(logsDir);
    }

    public String generateLogFile() {
        String id = UUID.randomUUID().toString();
        statusMap.put(id, Status.IN_PROGRESS);
        CompletableFuture.runAsync(() -> generateFile(id));
        return id;
    }

    private void generateFile(String id) {
        try {
            Thread.sleep(15000);

            Path source = Paths.get("application.log");
            Path output = logsDir.resolve(id + ".log");
            Files.copy(source, output);

            filePaths.put(id, output);
            statusMap.put(id, Status.DONE);
        } catch (Exception e) {
            statusMap.put(id, Status.FAILED);
        }
    }

    public Status getStatus(String id) {
        return statusMap.get(id);
    }

    public Path getFilePath(String id) {
        return filePaths.get(id);
    }
}
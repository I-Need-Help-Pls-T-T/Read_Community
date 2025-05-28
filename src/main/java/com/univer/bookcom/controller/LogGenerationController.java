package com.univer.bookcom.controller;

import com.univer.bookcom.service.LogGenerationService;
import com.univer.bookcom.service.LogGenerationService.Status;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log-generator")
@Tag(name = "Асинхронное создание логов", description = "Генерация логов по запросу")
public class LogGenerationController {

    private final LogGenerationService logService;

    public LogGenerationController(LogGenerationService logService) {
        this.logService = logService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startLogGeneration() {
        String id = logService.generateLogFile();
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> checkStatus(@RequestParam String id) {
        Status status = logService.getStatus(id);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "ID не найден"));
        }
        return ResponseEntity.ok(Map.of("status", status.name()));
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(@RequestParam String id) {
        Status status = logService.getStatus(id);

        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "ID не найден"));
        }

        if (status != Status.DONE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Файл не готов"));
        }

        Path file = logService.getFilePath(id);
        if (file == null || !file.toFile().exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Файл не найден"));
        }

        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + file.getFileName())
                .body(resource);
    }
}
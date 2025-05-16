package com.univer.bookcom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Управление логами", description = "API для работы с логами приложения")
public class LogController {

    private static final String LOG_FILE_PATH = "application.log";
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[(.*?)\\]"
                    + " (INFO|DEBUG|WARN|ERROR) (.*?)(?: - (.*))?$"
    );

    @Operation(summary = "Получить логи по дате",
            description = "Возвращает основные логи за указанную дату в компактном формате",
            responses = {
                @ApiResponse(responseCode = "200", description = "Логи успешно получены",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный формат даты"),
                @ApiResponse(responseCode = "404", description = "Файл логов не найден"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    @GetMapping("/by-date")
    public ResponseEntity<Map<String, Object>> getLogsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Path path = Paths.get(LOG_FILE_PATH);
            Map<String, Object> response = new HashMap<>();
            response.put("дата", date.toString());

            if (!Files.exists(path)) {
                response.put("ошибка", "Файл логов не найден");
                response.put("путь", path.toAbsolutePath().toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String datePrefix = date.toString();
            List<String> filteredLogs = new ArrayList<>();

            Files.lines(path)
                    .filter(line -> line.startsWith(datePrefix))
                    .forEach(line -> {
                        String simplified = simplifyLogEntry(line);
                        if (shouldIncludeLog(simplified)) {
                            filteredLogs.add(simplified);
                        }
                    });

            if (filteredLogs.isEmpty()) {
                response.put("сообщение", "Логи за указанную дату не найдены");
                return ResponseEntity.ok(response);
            }

            response.put("логи", filteredLogs);
            response.put("количество", filteredLogs.size());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ошибка", "Ошибка чтения логов");
            errorResponse.put("причина", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private boolean shouldIncludeLog(String logEntry) {
        return logEntry.contains("http-nio")
                || logEntry.contains("ERROR")
                || logEntry.contains("WARN")
                || logEntry.contains("CACHE")
                || logEntry.contains("Aspect")
                || logEntry.contains("Controller");
    }

    private String simplifyLogEntry(String logEntry) {
        Matcher matcher = LOG_PATTERN.matcher(logEntry);
        if (matcher.matches()) {
            String time = matcher.group(0).split(" ")[1];
            String thread = matcher.group(1);
            String level = matcher.group(2);
            String message = matcher.group(4) != null ? matcher.group(4) : matcher.group(3);

            message = message.replaceAll("org\\.springframework\\.", "")
                    .replaceAll("com\\.univer\\.bookcom\\.", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            return String.format("%s [%s] %s: %s",
                    time,
                    thread.replace("http-nio-8080-exec-", "req-"),
                    level,
                    message);
        }
        return logEntry;
    }
}
package com.univer.bookcom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
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

    @Operation(summary = "Получить структурированные логи", description =
            "Возвращает все логи в структурированном формате", responses = {
                @ApiResponse(responseCode = "200", description = "Логи успешно получены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = Map.class,
                                    example =
                            "{\"timestamp\":\"2023-01-01 12:00:00.000\",\"thread\":\"[main]\","
                            + "\"level\":\"INFO\",\"message\":\"Приложение запущено\"}")))),
                @ApiResponse(responseCode = "404", description = "Файл логов не найден",
                            content = @Content(schema = @Schema(example =
                            "{\"ошибка\":\"Файл логов не найден\","
                            + "\"path\":\"/path/to/log/file.log\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(example =
                            "{\"ошибка\":\"Ошибка чтения файла логов\","
                            + "\"details\":\"Сообщение IOException\"}")))
            })
    @GetMapping
    public ResponseEntity<?> getStructuredLogs() {
        try {
            Path path = Paths.get(LOG_FILE_PATH);

            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body(
                        Map.of("ошибка", "Файл логов не найден",
                                "path", path.toAbsolutePath().toString())
                );
            }

            List<String> logLines = Files.readAllLines(path);
            List<Map<String, String>> structuredLogs = new ArrayList<>();

            for (String line : logLines) {
                try {
                    Map<String, String> logEntry = new HashMap<>();

                    if (line.length() >= 23) {
                        logEntry.put("timestamp", line.substring(0, 23));

                        int threadEnd = line.indexOf("]", 24);
                        if (threadEnd > 0) {
                            logEntry.put("thread", line.substring(24, threadEnd + 1));

                            String[] parts = line.substring(threadEnd + 1).trim().split("\\s+");
                            if (parts.length >= 2) {
                                logEntry.put("level", parts[0]);

                                int messageStart = line.indexOf(" - ");
                                if (messageStart > 0) {
                                    logEntry.put("message", line.substring(messageStart + 3));
                                } else {
                                    logEntry.put("message", line.substring(threadEnd + 1).trim());
                                }
                            }
                        }
                    } else {
                        logEntry.put("raw", line);
                    }

                    structuredLogs.add(logEntry);

                } catch (Exception e) {
                    Map<String, String> errorEntry = new HashMap<>();
                    errorEntry.put("ошибка", "Ошибка парсинга строки лога");
                    errorEntry.put("raw", line);
                    structuredLogs.add(errorEntry);
                }
            }

            return ResponseEntity.ok(structuredLogs);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(
                    Map.of("ошибка", "Ошибка чтения файла логов",
                            "details", e.getMessage())
            );
        }
    }

    @Operation(summary = "Получить логи по дате",
            description = "Возвращает все логи за указанную дату в формате JSON",
            responses = {
                @ApiResponse(responseCode = "200", description = "Логи успешно получены",
                            content = @Content(schema = @Schema(implementation = Map.class,
                                    example = """
                      {
                        "дата": "2023-01-01",
                        "логи": [
                          "2023-01-01 12:00:00.000 [main] INFO Приложение запущено",
                          "2023-01-01 12:01:00.000 [main] DEBUG Загрузка конфигурации"
                        ],
                        "количество": 2
                      }"""))),
                @ApiResponse(responseCode = "400", description = "Некорректный формат даты",
                            content = @Content(schema = @Schema(example = """
                      {
                        "ошибка": "Некорректный формат даты",
                        "ожидаемый_формат": "yyyy-MM-dd"
                      }"""))),
                @ApiResponse(responseCode = "404", description = "Файл логов не найден",
                            content = @Content(schema = @Schema(example = """
                      {
                        "ошибка": "Файл логов не найден",
                        "путь": "/var/log/application.log"
                      }"""))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(example = """
                      {
                        "ошибка": "Ошибка чтения логов",
                        "причина": "Файл недоступен"
                      }""")))
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
                return ResponseEntity.status(404).body(response);
            }

            String datePrefix = date.toString();
            List<String> filteredLogs = Files.lines(path)
                    .filter(line -> line.startsWith(datePrefix))
                    .collect(Collectors.toList());

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
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
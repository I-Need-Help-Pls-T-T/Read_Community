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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String datePrefix = date.toString();
            List<String> filteredLogs = Files.lines(path)
                    .filter(line -> line.startsWith(datePrefix))
                    .toList();

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
}
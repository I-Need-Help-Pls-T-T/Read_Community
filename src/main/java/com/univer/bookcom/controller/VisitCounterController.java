package com.univer.bookcom.controller;

import com.univer.bookcom.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@Tag(name = "Счётчик посещений", description = "API для учёта посещений сайта")
public class VisitCounterController {

    private final VisitCounterService visitCounterService;

    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Operation(
            summary = "Получить все показатели посещений",
            description = "Возвращает карту с показателями посещений по всем методам",
            responses = {
                @ApiResponse(responseCode = "200", description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @GetMapping
    public Map<String, Integer> getAllVisitCounts() {
        return visitCounterService.getAllCounts();
    }

    @Operation(
            summary = "Получить показатель посещений для метода",
            description = "Возвращает количество посещений по имени метода",
            responses = {
                @ApiResponse(responseCode = "200", description = "Успешный ответ",
                            content = @Content(schema = @Schema(implementation = Integer.class))),
                @ApiResponse(responseCode = "400", description = "Неверный параметр methodName")
            }
    )
    @GetMapping("/method")
    public ResponseEntity<Integer> getVisitCountForMethod(@RequestParam String methodName) {
        int count = visitCounterService.getCount(methodName);
        return ResponseEntity.ok(count);
    }
}
package com.univer.bookcom.controller;

import com.univer.bookcom.exception.CommentNotFoundException;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.service.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
@Validated
@Tag(name = "Управление комментариями", description = "API для управления комментариями")
public class CommentsController {
    private static final Logger log = LoggerFactory.getLogger(CommentsController.class);
    private final CommentsService commentsService;

    public CommentsController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    @Operation(summary = "Создать комментарий",
            responses = {
                @ApiResponse(responseCode = "201", description = "Комментарий успешно создан",
                            content = @Content(schema = @Schema(implementation = Comments.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные входные данные\"}"))),
                @ApiResponse(responseCode = "404",
                        description = "Книга или пользователь не найдены",
                            content = @Content(schema = @Schema(example =
                                    "{\"ошибка\":\"Книга или пользователь не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping
    public ResponseEntity<Comments> createComment(
            @RequestParam @Positive
                    (message = "ID книги должен быть положительным числом") Long bookId,
            @RequestParam @Positive
                    (message = "ID пользователя должен быть положительным числом") Long userId,
            @RequestParam @NotBlank
                    (message = "Текст комментария не может быть пустым") String text) {
        log.debug("Запрос на создание комментария для книги ID: {}, пользователя ID: {}",
                bookId, userId);
        Comments comment = commentsService.createComment(bookId, userId, text);
        log.info("Создан комментарий ID: {}", comment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @Operation(summary = "Получить комментарии по книге",
            responses = {
                @ApiResponse(responseCode = "200", description = "Комментарии найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = Comments.class)))),
                @ApiResponse(responseCode = "404", description = "Комментарии не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Комментарии не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Comments>> getCommentsByBookId(
            @PathVariable @Positive
                    (message = "ID книги должен быть положительным числом") Long bookId) {
        log.debug("Запрос комментариев для книги ID: {}", bookId);
        List<Comments> comments = commentsService.getCommentsByBookId(bookId);

        if (comments.isEmpty()) {
            log.warn("Комментарии для книги ID: {} не найдены", bookId);
            throw new CommentNotFoundException("Комментарии не найдены");
        }

        log.info("Найдено {} комментариев для книги ID: {}", comments.size(), bookId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Получить комментарии по пользователю",
            responses = {
                @ApiResponse(responseCode = "200", description = "Комментарии найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = Comments.class)))),
                @ApiResponse(responseCode = "404", description = "Комментарии не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Комментарии не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comments>> getCommentsByUserId(@PathVariable @Positive
                    (message = "ID пользователя должен быть положительным числом") Long userId) {
        log.debug("Запрос комментариев пользователя ID: {}", userId);
        List<Comments> comments = commentsService.getCommentsByUserId(userId);

        if (comments.isEmpty()) {
            log.warn("Комментарии пользователя ID: {} не найдены", userId);
            throw new CommentNotFoundException("Комментарии не найдены");
        }

        log.info("Найдено {} комментариев пользователя ID: {}", comments.size(), userId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Обновить комментарий",
            responses = {
                @ApiResponse(responseCode = "200", description = "Комментарий успешно обновлен",
                            content = @Content(schema = @Schema(implementation = Comments.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные входные данные\"}"))),
                @ApiResponse(responseCode = "404", description = "Комментарий не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Комментарий не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PutMapping("/{commentId}")
    public ResponseEntity<Comments> updateComment(@PathVariable @Positive
                    (message = "ID комментария должен быть положительным числом") Long commentId,
            @RequestParam @NotBlank
                    (message = "Текст комментария не может быть пустым") String newText) {
        log.debug("Запрос на обновление комментария ID: {}", commentId);
        Comments updatedComment = commentsService.updateComment(commentId, newText);
        log.info("Комментарий ID: {} успешно обновлен", commentId);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Удалить комментарий",
            responses = {
                @ApiResponse(responseCode = "204", description = "Комментарий успешно удален"),
                @ApiResponse(responseCode = "404", description = "Комментарий не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Комментарий не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable @Positive
                    (message = "ID комментария должен быть положительным числом") Long commentId) {
        log.debug("Запрос на удаление комментария ID: {}", commentId);
        commentsService.deleteComment(commentId);
        log.info("Комментарий ID: {} успешно удален", commentId);
        return ResponseEntity.noContent().build();
    }
}
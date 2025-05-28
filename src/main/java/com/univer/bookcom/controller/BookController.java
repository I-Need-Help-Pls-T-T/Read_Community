package com.univer.bookcom.controller;

import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.CustomValidationException;
import com.univer.bookcom.exception.InvalidStatusException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.dto.request.BookRequestDto;
import com.univer.bookcom.model.dto.response.BookResponseDto;
import com.univer.bookcom.service.BookService;
import com.univer.bookcom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/books")
@Tag(name = "Управление книгами", description = "API для управления книгами")
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final UserService userService;

    private static final String BOOK_NOT_FOUND_BY_ID_MSG = "Книга с ID {} не найдена";
    private static final String BOOK_NOT_FOUND_MSG = "Книга не найдена";

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @Operation(summary = "Получить все книги",
            responses = {
                @ApiResponse(responseCode = "200", description = "Список книг получен",
                            content = @Content(schema = @Schema(implementation = List.class))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        log.debug("Запрос всех книг");
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(summary = "Получить книгу по ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга найдена",
                            content = @Content(schema =
                            @Schema(implementation = BookResponseDto.class))),
                @ApiResponse(responseCode = "404", description = BOOK_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга не найдена\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable @Positive Long id) {
        log.debug("Запрос книги по ID {}", id);
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn(BOOK_NOT_FOUND_BY_ID_MSG, id);
                    return new BookNotFoundException(BOOK_NOT_FOUND_MSG);
                });
    }

    @Operation(summary = "Создать книгу с автором",
            responses = {
                @ApiResponse(responseCode = "201", description = "Книга успешно создана",
                            content = @Content(schema =
                            @Schema(implementation = BookResponseDto.class))),
                @ApiResponse(responseCode = "404", description = "Автор не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Автор не найден\"}"))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/{authorId}/book")
    public ResponseEntity<BookResponseDto> createBookWithAuthor(
            @PathVariable @Positive Long authorId,
            @Valid @RequestBody BookRequestDto bookRequestDto) {
        log.debug("Создание книги с автором {}", authorId);
        BookResponseDto savedBook = bookService.createBookWithAuthor(authorId, bookRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @Operation(summary = "Обновить книгу",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга успешно обновлена",
                            content = @Content(schema =
                            @Schema(implementation = BookResponseDto.class))),
                @ApiResponse(responseCode = "404", description = BOOK_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга не найдена\"}"))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable @Positive Long id,
            @Valid @RequestBody BookRequestDto updatedBookDto) {
        log.debug("Обновление книги с ID {}", id);
        BookResponseDto updatedBook = bookService.updateBook(id, updatedBookDto);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Удалить книгу",
            responses = {
                @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
                @ApiResponse(responseCode = "404", description = BOOK_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга не найдена\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable @Positive Long id) {
        log.debug("Удаление книги с ID {}", id);
        if (!bookService.isCachedOrExists(id)) {
            throw new BookNotFoundException(BOOK_NOT_FOUND_MSG);
        }
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск книг по названию",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены",
                            content = @Content(schema = @Schema(implementation = List.class))),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книги не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/title")
    public ResponseEntity<List<BookResponseDto>> searchBooksByTitle(
            @RequestParam @NotBlank String title) {
        log.debug("Выполняется поиск книг по названию: {}", title);
        List<BookResponseDto> books = bookService.findBooksByTitle(title);
        if (books.isEmpty()) {
            throw new BookNotFoundException("Книги не найдены");
        }
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по автору",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены",
                            content = @Content(schema = @Schema(implementation = List.class))),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книги не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/author")
    public ResponseEntity<List<BookResponseDto>> searchBooksByAuthor(
            @RequestParam @NotBlank String author) {
        log.debug("Выполняется поиск книг по автору: {}", author);
        List<BookResponseDto> books = bookService.findBooksByAuthor(author);
        if (books.isEmpty()) {
            throw new BookNotFoundException("Книги не найдены");
        }
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по году публикации",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены",
                            content = @Content(schema = @Schema(implementation = List.class))),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книги не найдены\"}"))),
                @ApiResponse(responseCode = "400", description = "Некорректный год",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный год\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/year")
    public ResponseEntity<List<BookResponseDto>> searchBooksByYear(
            @RequestParam Long year) {
        if (year == null || year < 1000 || year > 2100) {
            Map<String, String> errors = new HashMap<>();
            if (year == null) {
                errors.put("year", "Год не может быть null");
            } else if (year < 1000) {
                errors.put("year", "Год должен быть не меньше 1000");
            } else {
                errors.put("year", "Год должен быть не больше 2100");
            }
            throw new CustomValidationException(errors);
        }
        log.debug("Поиск книг по году публикации: {}", year);
        List<BookResponseDto> books = bookService.findBooksByPublicYear(year);
        if (books.isEmpty()) {
            throw new BookNotFoundException("Книги не найдены");
        }
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по статусу",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены",
                            content = @Content(schema = @Schema(implementation = List.class))),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книги не найдены\"}"))),
                @ApiResponse(responseCode = "400", description = "Некорректный статус",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный статус\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/status")
    public ResponseEntity<List<BookResponseDto>> searchBooksByStatus(
            @RequestParam @NotBlank(message = "Статус книги не может быть пустым")
            String bookStatus) {
        log.debug("Обработка поиска книг по статусу: {}", bookStatus);
        try {
            List<BookResponseDto> books = bookService.findBooksByStatus(bookStatus);
            if (books.isEmpty()) {
                log.warn("Книги с указанным статусом не найдены");
                throw new BookNotFoundException("Книги не найдены");
            }
            log.info("Найдено {} книг с указанным статусом", books.size());
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            log.error("Некорректный статус: {}", bookStatus);
            throw new InvalidStatusException("Некорректный статус: " + bookStatus);
        }
    }

    @Operation(summary = "Добавить автора к книге",
            responses = {
                @ApiResponse(responseCode = "204", description = "Автор успешно добавлен"),
                @ApiResponse(responseCode = "404", description = "Книга или автор не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга или автор не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> addAuthorToBook(
            @PathVariable @Positive(message = "ID книги должен быть положительным числом")
            Long bookId,
            @PathVariable @Positive(message = "ID автора должен быть положительным числом")
            Long authorId) {
        log.debug("Обработка добавления автора к книге");

        if (!bookService.isCachedOrExists(bookId)) {
            log.error(BOOK_NOT_FOUND_BY_ID_MSG, bookId);
            throw new BookNotFoundException(BOOK_NOT_FOUND_MSG);
        }

        if (!userService.getUserById(authorId).isPresent()) {
            log.error("Автор с ID {} не найден", authorId);
            throw new UserNotFoundException("Автор не найден");
        }

        bookService.addAuthorToBook(bookId, authorId);
        log.info("Автор с ID {} успешно добавлен к книге с ID {}", authorId, bookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить автора из книги",
            responses = {
                @ApiResponse(responseCode = "204", description = "Автор успешно удален"),
                @ApiResponse(responseCode = "404", description = "Книга или автор не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга или автор не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> removeAuthorFromBook(
            @PathVariable @Positive(message = "ID книги должен быть положительным числом")
            Long bookId,
            @PathVariable @Positive(message = "ID автора должен быть положительным числом")
            Long authorId) {
        log.debug("Обработка удаления автора из книги");

        if (!bookService.isCachedOrExists(bookId)) {
            log.error(BOOK_NOT_FOUND_BY_ID_MSG, bookId);
            throw new BookNotFoundException(BOOK_NOT_FOUND_MSG);
        }

        if (!userService.getUserById(authorId).isPresent()) {
            log.error("Автор с ID {} не найден", authorId);
            throw new UserNotFoundException("Автор не найден");
        }

        bookService.removeAuthorFromBook(bookId, authorId);
        log.info("Автор с ID {} успешно удален из книги с ID {}", authorId, bookId);
        return ResponseEntity.noContent().build();
    }
}
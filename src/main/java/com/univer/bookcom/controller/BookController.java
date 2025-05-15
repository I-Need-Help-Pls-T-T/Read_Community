package com.univer.bookcom.controller;

import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.InvalidStatusException;
import com.univer.bookcom.exception.InvalidYearException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.service.BookService;
import com.univer.bookcom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    public ResponseEntity<List<Book>> getAllBooks() {
        log.debug("Запрос всех книг");
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(summary = "Получить книгу по ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга найдена",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                @ApiResponse(responseCode = "404", description = BOOK_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга не найдена\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable @Positive Long id) {
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
                            content = @Content(schema = @Schema(implementation = Book.class))),
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
    public ResponseEntity<Book> createBookWithAuthor(
            @PathVariable @Positive Long authorId,
            @Valid @RequestBody Book book) {
        log.debug("Создание книги с автором {}", authorId);
        Book savedBook = bookService.createBookWithAuthor(authorId, book);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @Operation(summary = "Обновить книгу",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга успешно обновлена",
                            content = @Content(schema = @Schema(implementation = Book.class))),
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
    public ResponseEntity<Book> updateBook(
            @PathVariable @Positive Long id,
            @Valid @RequestBody Book updatedBook) {
        log.debug("Обновление книги с ID {}", id);
        Book book = bookService.updateBook(id, updatedBook);
        return ResponseEntity.ok(book);
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
    public ResponseEntity<List<Book>> searchBooksByTitle(
            @RequestParam @NotBlank String title) {
        log.debug("Выполняется поиск книг по названию.");
        List<Book> books = bookService.findBooksByTitle(title);
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
    public ResponseEntity<List<Book>> searchBooksByAuthor(
            @RequestParam @NotBlank String author) {
        log.debug("Выполняется поиск книг по автору");
        List<Book> books = bookService.findBooksByAuthor(author);
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
    public ResponseEntity<List<Book>> searchBooksByYear(
            @RequestParam
            @Min(value = 1000, message = "Год должен быть не меньше 1000")
            @Max(value = 2100, message = "Год должен быть не больше 2100")
            Long year) {
        log.debug("Обработка поиска книг по году");

        if (year == null) {
            log.error("Некорректный год: null");
            throw new InvalidYearException("Год не может быть null");
        }

        List<Book> books = bookService.findBooksByPublicYear(year);

        if (books.isEmpty()) {
            log.warn("Книги за указанный год не найдены");
            throw new BookNotFoundException("Книги не найдены");
        }

        log.info("Найдено {} книг за указанный год", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по статусу",
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
    @GetMapping("/search/status")
    public ResponseEntity<List<Book>> searchBooksByStatus(@RequestParam @NotNull
            (message = "Статус книги не может быть null") BookStatus bookStatus) {
        log.debug("Обработка поиска книг по статусу");

        if (bookStatus == null) {
            log.error("Некорректный статус: null");
            throw new InvalidStatusException("Статус не может быть null");
        }

        List<Book> books = bookService.findBooksByStatus(bookStatus);

        if (books.isEmpty()) {
            log.warn("Книги с указанным статусом не найдены");
            throw new BookNotFoundException("Книги не найдены");
        }

        log.info("Найдено {} книг с указанным статусом", books.size());
        return ResponseEntity.ok(books);
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

        Book book = bookService.getBookById(bookId).orElseThrow(() -> {
            log.error(BOOK_NOT_FOUND_BY_ID_MSG, bookId);
            return new BookNotFoundException(BOOK_NOT_FOUND_MSG);
        });

        User author = userService.getUserById(authorId).orElseThrow(() -> {
            log.error("Автор с ID {} не найден", authorId);
            return new UserNotFoundException("Автор не найден");
        });

        book.addAuthor(author);
        bookService.saveBook(book);

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

        Book book = bookService.getBookById(bookId).orElseThrow(() -> {
            log.error(BOOK_NOT_FOUND_BY_ID_MSG, bookId);
            return new BookNotFoundException(BOOK_NOT_FOUND_MSG);
        });

        User author = userService.getUserById(authorId).orElseThrow(() -> {
            log.error("Автор с ID {} не найден", authorId);
            return new UserNotFoundException("Автор не найден");
        });

        book.removeAuthor(author);

        if (book.getAuthors().isEmpty()) {
            log.info("Удаление книги без авторов");
            bookService.deleteBook(bookId);
        } else {
            bookService.saveBook(book);
            log.info("Автор с ID {} успешно удален из книги с ID {}", authorId, bookId);
        }

        return ResponseEntity.noContent().build();
    }
}
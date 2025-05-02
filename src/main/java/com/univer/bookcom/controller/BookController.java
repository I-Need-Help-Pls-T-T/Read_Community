package com.univer.bookcom.controller;

import com.univer.bookcom.exception.BookNotFoundException;
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

@RestController
@RequestMapping("/books")
@Validated
@Tag(name = "Book Management", description = "API для управления книгами")
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @Operation(summary = "Запрос всех книг", description = "Вернуть все существующие книги",
            responses = {
                @ApiResponse(responseCode = "200", description = "Вернулись все книги"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        log.debug("Запрос всех книг");
        List<Book> books = bookService.getAllBooks();
        log.info("Найдено {} книг", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Запрос книг по id", description = "Вернуть книги по id",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга найдена"),
                @ApiResponse(responseCode = "404", description = "Книга не найдена",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Книга не найдена\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable @Positive
            (message = "ID книги должен быть положительным числом") Long id) {
        log.debug("Запрос книги с ID: {}", id);

        if (!bookService.isCachedOrExists(id)) {
            log.warn("Книга с ID {} не найдена", id);
            throw new BookNotFoundException("Книга с ID " + id + " не найдена");
        }

        Book book = bookService.getBookById(id)
                .orElseThrow(() -> {
                    log.error("Книга с ID {} не найдена в сервисе", id);
                    return new BookNotFoundException("Книга с id " + id + " не найдена");
                });

        log.info("Найдена книга: {}", book);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Создать книгу с автором", description =
            "Создать новую книгу с определенным автором", responses = {
                @ApiResponse(responseCode = "201", description = "Книга успешно создана"),
                @ApiResponse(responseCode = "404", description = "Автор не найден",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Автор не найден\" }"))),
                @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректные входные данные\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @PostMapping("/{authorId}/book")
    public ResponseEntity<Book> createBookWithAuthor(
            @PathVariable @Positive(message = "ID автора должен быть положительным числом")
            Long authorId, @Valid @RequestBody Book book) {
        log.debug("Создание книги {} для автора с ID {}", book, authorId);

        Book savedBook = bookService.createBookWithAuthor(authorId, book);

        log.info("Создана новая книга с ID: {}", savedBook.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @Operation(summary = "Обновить книгу", description = "Обновить существующую книгу",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга успешно обновлена"),
                @ApiResponse(responseCode = "404", description = "Книга не найдена",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Книга не найдена\" }"))),
                @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректные входные данные\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable @Positive(message = "ID книги должен быть положительным числом") Long id,
            @Valid @RequestBody Book updatedBook) {
        log.debug("Обновление книги с ID {}: {}", id, updatedBook);

        Book book = bookService.updateBook(id, updatedBook);

        log.info("Книга с ID {} успешно обновлена", id);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Удаление книги", description = "Удаление книги по id",
            responses = {
                @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
                @ApiResponse(responseCode = "404", description = "Книга не найдена",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Book not found\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable @Positive(message =
            "ID книги должен быть положительным числом") Long id) {
        log.debug("Удаление книги с ID: {}", id);

        if (!bookService.isCachedOrExists(id)) {
            log.warn("Попытка удаления несуществующей книги с ID {}", id);
            throw new BookNotFoundException("Книга с ID " + id + " не найдена");
        }

        bookService.deleteBook(id);
        log.info("Книга с ID {} успешно удалена", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск книг по названию", description =
            "Вернуть книги по названию", responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены"),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Books not found\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск книг по названию: {}", title);

        List<Book> books = bookService.findBooksByTitle(title);

        if (books.isEmpty()) {
            log.warn("Книги с названием '{}' не найдены", title);
            throw new BookNotFoundException("Книги с названием '" + title + "' не найдены");
        }

        log.info("Найдено {} книг с названием '{}'", books.size(), title);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по автору", description =
            "Вернуть книги по автору", responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены"),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Книги не найдены\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(
            @RequestParam @NotBlank(message = "Имя автора не может быть пустым") String author) {
        log.debug("Поиск книг по автору: {}", author);

        List<Book> books = bookService.findBooksByAuthor(author);

        if (books.isEmpty()) {
            log.warn("Книги автора '{}' не найдены", author);
            throw new BookNotFoundException("Книги автора '" + author + "' не найдены");
        }

        log.info("Найдено {} книг автора '{}'", books.size(), author);
        return ResponseEntity.ok(books);
    }

    // ИЗМЕНИТЬ ФИЛЬТРАЦИЮ ПО ГОДУ ПУБЛИКАЦИИ!!!!!!!!!!!!!!!!!!!!
    @Operation(summary = "Поиск по дате публикации", description =
            "Вернуть книги по дате публикации", responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены"),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Книги не найдены\" }"))),
                @ApiResponse(responseCode = "400",
                        description = "Некорректный ввод года публикации",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/year")
    public ResponseEntity<List<Book>> searchBooksByYear(
            @RequestParam
            @Min(value = 1000, message = "Год должен быть не меньше 1000")
            @Max(value = 2100, message = "Год должен быть не больше 2100")
            Long year) {
        log.debug("Поиск книг по году публикации: {}", year);

        List<Book> books = bookService.findBooksByPublicYear(year);

        if (books.isEmpty()) {
            log.warn("Книги за {} год не найдены", year);
            throw new BookNotFoundException("Книги за " + year + " год не найдены");
        }

        log.info("Найдено {} книг за {} год", books.size(), year);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск по статусу выхода книги",
            description = "Вернуть книги по статусу выхода", responses = {
                @ApiResponse(responseCode = "200", description = "Книги найдены"),
                @ApiResponse(responseCode = "404", description = "Книги не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Книги не найдены\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/status")
    public ResponseEntity<List<Book>> searchBooksByStatus(@RequestParam @NotNull
            (message = "Статус книги не может быть null") BookStatus bookStatus) {
        log.debug("Поиск книг по статусу: {}", bookStatus);

        List<Book> books = bookService.findBooksByStatus(bookStatus);

        if (books.isEmpty()) {
            log.warn("Книги со статусом '{}' не найдены", bookStatus);
            throw new BookNotFoundException("Книги со статусом '" + bookStatus + "' не найдены");
        }

        log.info("Найдено {} книг со статусом '{}'", books.size(), bookStatus);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Add author to bookДобавить автора к книге",
            description = "Добавление автора к книге", responses = {
                @ApiResponse(responseCode = "204", description = "Автор добавлен успешно"),
                @ApiResponse(responseCode = "404", description = "Книга или автор не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Книга или автор не найдены\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @PostMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> addAuthorToBook(@PathVariable @Positive
            (message = "ID книги должен быть положительным числом")
            Long bookId, @PathVariable @Positive
            (message = "ID автора должен быть положительным числом")
            Long authorId) {
        log.debug("Добавление автора {} к книге {}", authorId, bookId);

        Book book = bookService.getBookById(bookId).orElseThrow(() -> {
            log.error("Книга с ID {} не найдена", bookId);
            return new BookNotFoundException("Книга с id " + bookId + " не найдена");
        });

        User author = userService.getUserById(authorId).orElseThrow(() -> {
            log.error("Автор с ID {} не найден", authorId);
            return new UserNotFoundException("Автор с id " + authorId + " не найден");
        });

        book.addAuthor(author);
        bookService.saveBook(book);

        log.info("Автор {} успешно добавлен к книге {}", authorId, bookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить автора из книги",
            description = "Удаляет автора из определенной книги", responses = {
                @ApiResponse(responseCode = "204", description = "Автор успешно удален"),
                @ApiResponse(responseCode = "404", description = "Книга или автор не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Book or author not found\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @DeleteMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> removeAuthorFromBook(
            @PathVariable @Positive
                    (message = "ID книги должен быть положительным числом") Long bookId,
            @PathVariable @Positive
                    (message = "ID автора должен быть положительным числом") Long authorId) {
        log.debug("Удаление автора {} из книги {}", authorId, bookId);

        Book book = bookService.getBookById(bookId).orElseThrow(() -> {
            log.error("Книга с ID {} не найдена", bookId);
            return new BookNotFoundException("Книга с id " + bookId + " не найдена");
        });

        User author = userService.getUserById(authorId).orElseThrow(() -> {
            log.error("Автор с ID {} не найден", authorId);
            return new UserNotFoundException("Автор с id " + authorId + " не найден");
        });

        book.removeAuthor(author);

        if (book.getAuthors().isEmpty()) {
            log.info("Удаление книги {}, так как у нее не осталось авторов", bookId);
            bookService.deleteBook(bookId);
        } else {
            bookService.saveBook(book);
            log.info("Автор {} удален из книги {}", authorId, bookId);
        }

        return ResponseEntity.noContent().build();
    }
}
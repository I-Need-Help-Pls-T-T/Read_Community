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
@Tag(name = "Управление книгами", description = "API для управления книгами")
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @Operation(summary = "Получить все книги", description = "Возвращает список всех книг",
            responses = {
                @ApiResponse(responseCode = "200", description = "Список книг получен",
                            content = @Content(schema = @Schema(implementation = List.class))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        log.debug("Обработка запроса всех книг");
        List<Book> books = bookService.getAllBooks();
        log.info("Успешно возвращено {} книг", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Получить книгу по ID", description = "Возвращает книгу по указанному ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга найдена",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                @ApiResponse(responseCode = "404", description = "Книга не найдена",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга не найдена\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable @Positive
            (message = "ID книги должен быть положительным числом") Long id) {
        log.debug("Обработка запроса книги с ID");

        if (!bookService.isCachedOrExists(id)) {
            log.warn("Запрошенная книга не найдена");
            throw new BookNotFoundException("Книга не найдена");
        }

        Book book = bookService.getBookById(id)
                .orElseThrow(() -> {
                    log.error("Ошибка при поиске книги в сервисе");
                    return new BookNotFoundException("Книга не найдена");
                });

        log.info("Книга успешно найдена");
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Создать книгу с автором",
            description = "Создает новую книгу с указанным автором",
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
    public ResponseEntity<Book> createBookWithAuthor(@PathVariable @Positive
            (message = "ID автора должен быть положительным числом") Long authorId,
            @Valid @RequestBody Book book) {
        log.debug("Обработка создания новой книги");

        Book savedBook = bookService.createBookWithAuthor(authorId, book);

        log.info("Новая книга успешно создана");
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @Operation(summary = "Обновить книгу", description = "Обновляет существующую книгу",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книга успешно обновлена",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                @ApiResponse(responseCode = "404", description = "Книга не найдена",
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
            @PathVariable @Positive(message = "ID книги должен быть положительным числом") Long id,
            @Valid @RequestBody Book updatedBook) {
        log.debug("Обработка обновления книги");

        Book book = bookService.updateBook(id, updatedBook);

        log.info("Книга успешно обновлена");
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Удалить книгу", description = "Удаляет книгу по ID",
            responses = {
                @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
                @ApiResponse(responseCode = "404", description = "Книга не найдена",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Книга не найдена\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable @Positive
                    (message = "ID книги должен быть положительным числом") Long id) {
        log.debug("Обработка удаления книги");

        if (!bookService.isCachedOrExists(id)) {
            log.warn("Попытка удаления несуществующей книги");
            throw new BookNotFoundException("Книга не найдена");
        }

        bookService.deleteBook(id);
        log.info("Книга успешно удалена");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск книг по названию",
            description = "Возвращает книги по указанному названию",
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
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Обработка поиска книг по названию");

        List<Book> books = bookService.findBooksByTitle(title);

        if (books.isEmpty()) {
            log.warn("Книги по запросу не найдены");
            throw new BookNotFoundException("Книги не найдены");
        }

        log.info("Найдено {} книг по запросу", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по автору",
            description = "Возвращает книги по указанному автору",
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
            @RequestParam @NotBlank(message = "Имя автора не может быть пустым") String author) {
        log.debug("Обработка поиска книг по автору");

        List<Book> books = bookService.findBooksByAuthor(author);

        if (books.isEmpty()) {
            log.warn("Книги автора не найдены");
            throw new BookNotFoundException("Книги не найдены");
        }

        log.info("Найдено {} книг автора", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по году публикации",
            description = "Возвращает книги по указанному году публикации",
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

        List<Book> books = bookService.findBooksByPublicYear(year);

        if (books.isEmpty()) {
            log.warn("Книги за указанный год не найдены");
            throw new BookNotFoundException("Книги не найдены");
        }

        log.info("Найдено {} книг за указанный год", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Поиск книг по статусу",
            description = "Возвращает книги по указанному статусу",
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

        List<Book> books = bookService.findBooksByStatus(bookStatus);

        if (books.isEmpty()) {
            log.warn("Книги с указанным статусом не найдены");
            throw new BookNotFoundException("Книги не найдены");
        }

        log.info("Найдено {} книг с указанным статусом", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Добавить автора к книге",
            description = "Добавляет автора к указанной книге",
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
    public ResponseEntity<Void> addAuthorToBook(@PathVariable @Positive
                    (message = "ID книги должен быть положительным числом") Long bookId,
            @PathVariable @Positive
                    (message = "ID автора должен быть положительным числом") Long authorId) {
        log.debug("Обработка добавления автора к книге");

        Book book = bookService.getBookById(bookId).orElseThrow(() -> {
            log.error("Книга не найдена");
            return new BookNotFoundException("Книга не найдена");
        });

        User author = userService.getUserById(authorId).orElseThrow(() -> {
            log.error("Автор не найден");
            return new UserNotFoundException("Автор не найден");
        });

        book.addAuthor(author);
        bookService.saveBook(book);

        log.info("Автор успешно добавлен к книге");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить автора из книги",
            description = "Удаляет автора из указанной книги",
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
    public ResponseEntity<Void> removeAuthorFromBook(@PathVariable @Positive
            (message = "ID книги должен быть положительным числом") Long bookId,
            @PathVariable @Positive
            (message = "ID автора должен быть положительным числом") Long authorId) {
        log.debug("Обработка удаления автора из книги");

        Book book = bookService.getBookById(bookId).orElseThrow(() -> {
            log.error("Книга не найдена");
            return new BookNotFoundException("Книга не найдена");
        });

        User author = userService.getUserById(authorId).orElseThrow(() -> {
            log.error("Автор не найден");
            return new UserNotFoundException("Автор не найден");
        });

        book.removeAuthor(author);

        if (book.getAuthors().isEmpty()) {
            log.info("Удаление книги без авторов");
            bookService.deleteBook(bookId);
        } else {
            bookService.saveBook(book);
            log.info("Автор успешно удален из книги");
        }

        return ResponseEntity.noContent().build();
    }
}
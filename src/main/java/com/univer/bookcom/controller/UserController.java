package com.univer.bookcom.controller;

import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.UserRepository;
import com.univer.bookcom.service.BookService;
import com.univer.bookcom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "Управление пользователями", description = "API для управления пользователями")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String bookNot = " не найден";
    private final UserService userService;
    private final BookService bookService;
    private final UserRepository userRepository;

    public UserController(UserService userService, BookService bookService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.bookService = bookService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей", responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema
                                    (schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<User> users = userService.getAllUsers();
        log.info("Найдено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает пользователя по указанному ID", responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный ID",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректный ID\" }"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Пользователь не найден\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable @Positive
                    (message = "ID пользователя должен быть положительным числом") Long id) {
        log.debug("Запрос пользователя с ID: {}", id);

        User user = userService.getUserById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь с id " + id + " не найден");
                });

        log.info("Найден пользователь: {}", user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя",
            responses = {
                @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректные входные данные\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.debug("Создание нового пользователя: {}", user);

        User savedUser = userService.saveUser(user);

        log.info("Создан новый пользователь с ID: {}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет существующего пользователя по ID", responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400",
                        description = "Некорректный ID или входные данные", content = @Content
                        (schema = @Schema(example =
                                "{ \"ошибка\": \"Некорректный ID или входные данные\" }"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Пользователь не найден\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @Positive
            (message = "ID пользователя должен быть положительным числом") Long id,
            @Valid @RequestBody User updatedUser) {
        log.debug("Обновление пользователя с ID {}: {}", id, updatedUser);

        User user = userService.updateUser(id, updatedUser);

        log.info("Пользователь с ID {} успешно обновлен", id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID",
            responses = {
                @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
                @ApiResponse(responseCode = "400", description = "Некорректный ID",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректный ID\" }"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Пользователь не найден\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive
            (message = "ID пользователя должен быть положительным числом") Long id) {
        log.debug("Удаление пользователя с ID: {}", id);

        User author = userService.getUserById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь с id " + id + " не найден");
                });

        List<Book> books = bookService.findBooksByAuthor(author);
        log.debug("Найдено {} книг автора для удаления", books.size());

        for (Book book : books) {
            book.removeAuthor(author);
            if (book.getAuthors().isEmpty()) {
                log.debug("Удаление книги {} без авторов", book.getId());
                bookService.deleteBook(book.getId());
            } else {
                bookService.saveBook(book);
            }
        }

        userService.deleteUser(id);
        log.info("Пользователь с ID {} и все связанные данные успешно удалены", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск пользователей по имени",
            description = "Возвращает пользователей, соответствующих имени", responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema
                                    (schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректный параметр имени",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Имя не может быть пустым\" }"))),
                @ApiResponse(responseCode = "404", description = "Пользователи не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Пользователи не найдены\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/name")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam @NotBlank
            (message = "Имя пользователя не может быть пустым") String name) {
        log.debug("Поиск пользователей по имени: {}", name);

        List<User> users = userService.findUsersByName(name);

        if (users.isEmpty()) {
            log.warn("Пользователи с именем '{}' не найдены", name);
            throw new UserNotFoundException("Пользователи с именем '" + name + "' не найдены");
        }

        log.info("Найдено {} пользователей с именем '{}'", users.size(), name);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Поиск пользователя по email",
            description = "Возвращает пользователя по email", responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный формат email",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректный формат email\" }"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Пользователь не найден\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/email")
    public ResponseEntity<User> searchUserByEmail(
            @RequestParam @Email(message = "Некорректный формат email") String email) {
        log.debug("Поиск пользователя по email: {}", email);

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Пользователь с email '{}' не найден", email);
                    return new
                            UserNotFoundException("Пользователь с email " + email + " не найден");
                });

        log.info("Найден пользователь по email {}: {}", email, user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Добавить книгу пользователю",
            description = "Добавляет книгу в коллекцию пользователя", responses = {
                @ApiResponse(responseCode = "204", description = "Книга успешно добавлена"),
                @ApiResponse(responseCode = "400", description = "Некорректный ID или данные книги",
                            content = @Content(schema = @Schema(example =
                                    "{ \"ошибка\": \"Некорректный ID или данные книги\" }"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Пользователь не найден\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @PostMapping("/{userId}/books")
    public ResponseEntity<Void> addBookToUser(@PathVariable @Positive
                    (message = "ID пользователя должен быть положительным числом") Long userId,
            @Valid @RequestBody Book book) {
        log.debug("Добавление книги {} пользователю {}", book, userId);

        userService.addBookToUser(userId, book);

        log.info("Книга успешно добавлена пользователю с ID {}", userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить книгу у пользователя",
            description = "Удаляет книгу из коллекции пользователя", responses = {
                @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
                @ApiResponse(responseCode = "400", description = "Некорректный ID",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Некорректный ID\" }"))),
                @ApiResponse(responseCode = "404",
                        description = "Пользователь или книга не найдены",
                            content = @Content(schema = @Schema(example =
                                    "{ \"ошибка\": \"Пользователь или книга не найдены\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @DeleteMapping("/{userId}/books/{bookId}")
    public ResponseEntity<Void> removeBookFromUser(
            @PathVariable @Positive
                    (message = "ID пользователя должен быть положительным числом") Long userId,
            @PathVariable @Positive
                    (message = "ID книги должен быть положительным числом") Long bookId) {
        log.debug("Удаление книги {} у пользователя {}", bookId, userId);

        Book book = new Book();
        book.setId(bookId);
        userService.removeBookFromUser(userId, book);

        log.info("Книга {} успешно удалена у пользователя {}", bookId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить пользователей по названию книги",
            description = "Возвращает пользователей, связанных с названием книги", responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema
                                    (schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректный параметр названия",
                            content = @Content(schema = @Schema(example =
                                            "{ \"ошибка\": \"Название не может быть пустым\" }"))),
                @ApiResponse(responseCode = "404",
                        description = "Книга не найдена или нет связанных пользователей",
                            content = @Content(schema = @Schema(example =
                                    "{ \"ошибка\": \"Книга не найдена или нет пользователей\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/by-book-title")
    public ResponseEntity<List<User>> getUsersByBookTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск пользователей по названию книги: {}", title);

        List<User> users = userRepository.findUsersByBookTitle(title);

        if (users.isEmpty()) {
            log.warn("Книга с названием '{}' не найдена или у неё нет авторов", title);
            throw new UserNotFoundException("Книга с названием '"
                    + title + "' не найдена или у неё нет авторов");
        }

        log.info("Найдено {} пользователей по книге '{}'", users.size(), title);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить авторов по названию книги",
            description = "Возвращает авторов книги по названию", responses = {
                @ApiResponse(responseCode = "200", description = "Авторы найдены",
                            content = @Content(array = @ArraySchema
                                    (schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректный параметр названия",
                            content = @Content(schema = @Schema(example =
                                            "{ \"ошибка\": \"Название не может быть пустым\" }"))),
                @ApiResponse(responseCode = "404", description = "Авторы не найдены",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Авторы не найдены\" }"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema
                                    (example = "{ \"ошибка\": \"Внутренняя ошибка сервера\" }")))
            })
    @GetMapping("/search/by-book-title")
    public ResponseEntity<List<User>> getAuthorsByBookTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск авторов по названию книги: {}", title);

        List<User> authors = userRepository.findAuthorsByBookTitle(title);

        if (authors.isEmpty()) {
            log.warn("Авторы книги с названием '{}' не найдены", title);
            throw new UserNotFoundException("Авторы книги с названием '" + title + "' не найдены");
        }

        log.info("Найдено {} авторов книги '{}'", authors.size(), title);
        return ResponseEntity.ok(authors);
    }
}
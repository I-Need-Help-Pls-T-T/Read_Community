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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
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
            description = "Возвращает список всех пользователей",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<User> users = userService.getAllUsers();
        log.info("Успешно возвращено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает пользователя по указанному ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный ID",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный ID\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable @Positive
            (message = "ID пользователя должен быть положительным числом") Long id) {
        log.debug("Запрос пользователя по ID");

        User user = userService.getUserById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден");
                    return new UserNotFoundException("Пользователь не найден");
                });

        log.info("Пользователь успешно найден");
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Создать пользователя",
            description = "Создает нового пользователя",
            responses = {
                @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.debug("Создание нового пользователя");

        User savedUser = userService.saveUser(user);

        log.info("Пользователь успешно создан");
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет существующего пользователя",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь обновлен",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @Positive
                    (message = "ID пользователя должен быть положительным числом") Long id,
            @Valid @RequestBody User updatedUser) {
        log.debug("Обновление пользователя");

        User user = userService.updateUser(id, updatedUser);

        log.info("Пользователь успешно обновлен");
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя и связанные данные",
            responses = {
                @ApiResponse(responseCode = "204", description = "Пользователь удален"),
                @ApiResponse(responseCode = "400", description = "Некорректный ID",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный ID\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive
            (message = "ID пользователя должен быть положительным числом") Long id) {
        log.debug("Удаление пользователя");

        User author = userService.getUserById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден");
                    return new UserNotFoundException("Пользователь не найден");
                });

        List<Book> books = bookService.findBooksByAuthor(author);
        log.debug("Найдено {} связанных книг", books.size());

        books.forEach(book -> {
            book.removeAuthor(author);
            if (book.getAuthors().isEmpty()) {
                bookService.deleteBook(book.getId());
            } else {
                bookService.saveBook(book);
            }
        });

        userService.deleteUser(id);
        log.info("Пользователь и связанные данные успешно удалены");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск пользователей по имени",
            description = "Возвращает пользователей по имени",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректное имя",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Имя не может быть пустым\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователи не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователи не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/name")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam @NotBlank
            (message = "Имя пользователя не может быть пустым") String name) {
        log.debug("Поиск пользователей по имени");

        List<User> users = userService.findUsersByName(name);

        if (users.isEmpty()) {
            log.warn("Пользователи не найдены");
            throw new UserNotFoundException("Пользователи не найдены");
        }

        log.info("Найдено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Поиск пользователя по email",
            description = "Возвращает пользователя по email",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный email",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный формат email\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/email")
    public ResponseEntity<User> searchUserByEmail(
            @RequestParam @Email(message = "Некорректный формат email") String email) {
        log.debug("Поиск пользователя по email");

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден");
                    return new UserNotFoundException("Пользователь не найден");
                });

        log.info("Пользователь успешно найден");
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Добавить книгу пользователю",
            description = "Добавляет книгу в коллекцию пользователя",
            responses = {
                @ApiResponse(responseCode = "204", description = "Книга добавлена"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные книги\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/{userId}/books")
    public ResponseEntity<Void> addBookToUser(@PathVariable @Positive
            (message = "ID пользователя должен быть положительным числом") Long userId,
            @Valid @RequestBody Book book) {
        log.debug("Добавление книги пользователю");

        userService.addBookToUser(userId, book);

        log.info("Книга успешно добавлена");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить книгу у пользователя",
            description = "Удаляет книгу из коллекции пользователя",
            responses = {
                @ApiResponse(responseCode = "204", description = "Книга удалена"),
                @ApiResponse(responseCode = "400", description = "Некорректные ID",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные ID\"}"))),
                @ApiResponse(responseCode = "404", description = "Данные не найдены",
                            content = @Content(schema = @Schema(example =
                            "{\"ошибка\":\"Пользователь или книга не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{userId}/books/{bookId}")
    public ResponseEntity<Void> removeBookFromUser(
            @PathVariable @Positive
                    (message = "ID пользователя должен быть положительным числом") Long userId,
            @PathVariable @Positive
                    (message = "ID книги должен быть положительным числом") Long bookId) {
        log.debug("Удаление книги у пользователя");

        Book book = new Book();
        book.setId(bookId);
        userService.removeBookFromUser(userId, book);

        log.info("Книга успешно удалена");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить пользователей по книге",
            description = "Возвращает пользователей, связанных с книгой",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректное название",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Название не может быть пустым\"}"))),
                @ApiResponse(responseCode = "404", description = "Данные не найдены",
                            content = @Content(schema = @Schema(example =
                            "{\"ошибка\":\"Книга или пользователи не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/by-book-title")
    public ResponseEntity<List<User>> getUsersByBookTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск пользователей по книге");

        List<User> users = userRepository.findUsersByBookTitle(title);

        if (users.isEmpty()) {
            log.warn("Данные не найдены");
            throw new UserNotFoundException("Данные не найдены");
        }

        log.info("Найдено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить авторов книги",
            description = "Возвращает авторов книги по названию",
            responses = {
                @ApiResponse(responseCode = "200", description = "Авторы найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректное название",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Название не может быть пустым\"}"))),
                @ApiResponse(responseCode = "404", description = "Авторы не найдены",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Авторы не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/by-book-title")
    public ResponseEntity<List<User>> getAuthorsByBookTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск авторов книги");

        List<User> authors = userRepository.findAuthorsByBookTitle(title);

        if (authors.isEmpty()) {
            log.warn("Авторы не найдены");
            throw new UserNotFoundException("Авторы не найдены");
        }

        log.info("Найдено {} авторов", authors.size());
        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Создать несколько пользователей",
            description = "Создает несколько пользователей за один запрос",
            responses = {
                @ApiResponse(responseCode = "201", description = "Пользователи успешно созданы",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = User.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "409", description = "Конфликт email",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Email уже существует\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/bulk")
    public ResponseEntity<List<User>> createUsersBulk(@Valid @RequestBody List<User> users) {
        log.debug("Обработка bulk-запроса на создание {} пользователей", users.size());

        List<String> emails = users.stream().map(User::getEmail)
                .collect(Collectors.toList());

        if (emails.size() != new HashSet<>(emails).size()) {
            log.error("Обнаружены дубликаты email в запросе");
            throw new IllegalArgumentException("Email пользователей должны быть уникальными");
        }

        List<String> existingEmails = emails.stream()
                .filter(email -> userService.findUserByEmail(email).isPresent())
                .collect(Collectors.toList());

        if (!existingEmails.isEmpty()) {
            log.error("Email уже существуют: {}", existingEmails);
            throw new IllegalArgumentException("Email уже существуют: " + existingEmails);
        }

        List<User> createdUsers = users.stream()
                .map(user -> {
                    try {
                        return userService.saveUser(user);
                    } catch (Exception e) {
                        log.error("Ошибка при создании пользователя: {}", e.getMessage());
                        throw e;
                    }
                })
                .collect(Collectors.toList());

        log.info("Успешно создано {} пользователей", createdUsers.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
    }

    @Operation(summary = "Добавить несколько книг пользователю",
            description = "Добавляет несколько книг в коллекцию пользователя",
            responses = {
                @ApiResponse(responseCode = "204", description = "Книги успешно добавлены"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/{userId}/books/bulk")
    public ResponseEntity<Void> addBooksToUser(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long userId, @Valid @RequestBody List<Book> books) {
        log.debug("Обработка bulk-запроса на добавление {} книг пользователю", books.size());

        User user = userService.getUserById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден");
                    return new UserNotFoundException("Пользователь не найден");
                });

        books.forEach(book -> userService.addBookToUser(userId, book));

        log.info("Успешно добавлено {} книг пользователю", books.size());
        return ResponseEntity.noContent().build();
    }

}
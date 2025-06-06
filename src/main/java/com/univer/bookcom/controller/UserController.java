package com.univer.bookcom.controller;

import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.dto.request.BookRequestDto;
import com.univer.bookcom.model.dto.request.UserRequestDto;
import com.univer.bookcom.model.dto.response.BookResponseDto;
import com.univer.bookcom.model.dto.response.UserResponseDto;
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
import org.springframework.web.ErrorResponse;
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

    private static final String USER_NOT_FOUND_MSG = "Пользователь не найден";

    public UserController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = UserResponseDto.class)))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<UserResponseDto> users = userService.getAllUsersDto();
        log.info("Успешно возвращено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает пользователя по указанному ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema =
                            @Schema(implementation = UserResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный ID",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный ID\"}"))),
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long id) {
        log.debug("Запрос пользователя по ID {}", id);
        UserResponseDto user = userService.getUserByIdDto(id)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_MSG);
                    return new UserNotFoundException(USER_NOT_FOUND_MSG);
                });
        log.info("Пользователь успешно найден");
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Создать пользователя",
            description = "Создает нового пользователя",
            responses = {
                @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                            content = @Content(schema =
                            @Schema(implementation = UserResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userDto) {
        log.debug("Создание нового пользователя");
        UserResponseDto savedUser = userService.saveUserDto(userDto);
        log.info("Пользователь успешно создан");
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет существующего пользователя",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь обновлен",
                            content = @Content(schema =
                            @Schema(implementation = UserResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long id,
            @Valid @RequestBody UserRequestDto updatedUserDto) {
        log.debug("Обновление пользователя с ID {}", id);
        UserResponseDto user = userService.updateUserDto(id, updatedUserDto);
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
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long id) {
        log.debug("Удаление пользователя с ID {}", id);
        if (!userService.existsById(id)) {
            log.error(USER_NOT_FOUND_MSG);
            throw new UserNotFoundException(USER_NOT_FOUND_MSG);
        }
        userService.deleteUser(id);
        log.info("Пользователь и связанные данные успешно удалены");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск пользователей по имени",
            description = "Возвращает пользователей по имени",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = UserResponseDto.class)))),
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
    public ResponseEntity<List<UserResponseDto>> searchUsersByName(
            @RequestParam @NotBlank(message = "Имя пользователя не может быть пустым")
            String name) {
        log.debug("Поиск пользователей по имени: {}", name);
        List<UserResponseDto> users = userService.findUsersByNameDto(name);
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
                            content = @Content(schema =
                            @Schema(implementation = UserResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный email",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный формат email\"}"))),
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/search/email")
    public ResponseEntity<UserResponseDto> searchUserByEmail(
            @RequestParam @Email(message = "Некорректный формат email") String email) {
        log.debug("Поиск пользователя по email: {}", email);
        UserResponseDto user = userService.findUserByEmailDto(email)
                .orElseThrow(() -> {
                    log.warn(USER_NOT_FOUND_MSG);
                    return new UserNotFoundException(USER_NOT_FOUND_MSG);
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
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/{userId}/books")
    public ResponseEntity<Void> addBookToUser(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long userId,
            @Valid @RequestBody BookRequestDto bookDto) {
        log.debug("Добавление книги пользователю с ID {}", userId);
        userService.addBookToUserDto(userId, bookDto);
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
                            content = @Content(schema = @Schema(example
                                    = "{\"ошибка\":\"Пользователь или книга не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @DeleteMapping("/{userId}/books/{bookId}")
    public ResponseEntity<Void> removeBookFromUser(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long userId,
            @PathVariable @Positive(message = "ID книги должен быть положительным числом")
            Long bookId) {
        log.debug("Удаление книги с ID {} у пользователя с ID {}", bookId, userId);
        if (!userService.existsById(userId)) {
            log.error(USER_NOT_FOUND_MSG);
            throw new UserNotFoundException(USER_NOT_FOUND_MSG);
        }
        if (!bookService.isCachedOrExists(bookId)) {
            log.error("Книга с ID {} не найдена", bookId);
            throw new UserNotFoundException("Книга не найдена");
        }
        userService.removeBookFromUserDto(userId, bookId);
        log.info("Книга успешно удалена");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить пользователей по книге",
            description = "Возвращает пользователей, связанных с книгой",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = UserResponseDto.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректное название",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Название не может быть пустым\"}"))),
                @ApiResponse(responseCode = "404", description = "Данные не найдены",
                            content = @Content(schema = @Schema(example
                                    = "{\"ошибка\":\"Книга или пользователи не найдены\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/by-book-title")
    public ResponseEntity<List<UserResponseDto>> getUsersByBookTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск пользователей по книге: {}", title);
        List<UserResponseDto> users = userService.findUsersByBookTitleDto(title);
        if (users.isEmpty()) {
            log.warn("Данные не найдены");
            throw new UserNotFoundException("Книга или пользователи не найдены");
        }
        log.info("Найдено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить авторов книги",
            description = "Возвращает авторов книги по названию",
            responses = {
                @ApiResponse(responseCode = "200", description = "Авторы найдены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = UserResponseDto.class)))),
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
    public ResponseEntity<List<UserResponseDto>> getAuthorsByBookTitle(
            @RequestParam @NotBlank(message = "Название книги не может быть пустым") String title) {
        log.debug("Поиск авторов книги: {}", title);
        List<UserResponseDto> authors = userService.findAuthorsByBookTitleDto(title);
        if (authors.isEmpty()) {
            log.warn("Авторы не найдены");
            throw new UserNotFoundException("Авторы не найдены");
        }
        log.info("Найдено {} авторов", authors.size());
        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Добавить несколько книг пользователю",
            description = "Добавляет несколько книг пользователю. Книга не будет "
                    + "добавлена, если у пользователя уже есть книга с таким названием",
            responses = {
                @ApiResponse(responseCode = "200", description = "Книги успешно добавлены",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = BookResponseDto.class)))),
                @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректные данные\"}"))),
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @PostMapping("/{userId}/books/bulk")
    public ResponseEntity<List<BookResponseDto>> addBooksToUserBulk(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long userId,
            @Valid @RequestBody List<BookRequestDto> bookDtos) {
        log.debug("Добавление нескольких книг пользователю с ID {}", userId);
        List<BookResponseDto> addedBooks = userService.addBooksToUserBulkDto(userId, bookDtos);
        log.info("Успешно добавлено {} книг", addedBooks.size());
        return ResponseEntity.ok(addedBooks);
    }

    @Operation(summary = "Получить количество книг пользователя",
            responses = {
                @ApiResponse(responseCode = "200", description = "Количество получено",
                            content = @Content(schema = @Schema(implementation = Long.class))),
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}"))),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Внутренняя ошибка сервера\"}")))
            })
    @GetMapping("/{id}/published-count")
    public ResponseEntity<Long> getPublishedBooksCount(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом")
            Long id) {
        log.debug("Запрос количества книг для пользователя с ID {}", id);
        long count = userService.getPublishedBooksCountByUserId(id);
        log.info("Количество книг: {}", count);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Проверить пароль пользователя",
            description = "Проверяет, соответствует ли"
                    + "предоставленный пароль пользователю с указанным email",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пароль проверен",
                            content = @Content(schema = @Schema(implementation = Boolean.class))),
                @ApiResponse(responseCode = "400", description = "Некорректный email или пароль",
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Некорректный email или пароль\"}"))),
                @ApiResponse(responseCode = "404", description = USER_NOT_FOUND_MSG,
                            content = @Content(schema = @Schema(
                                    example = "{\"ошибка\":\"Пользователь не найден\"}")))
            })
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody
        @Valid PasswordVerificationRequest request) {
        log.debug("Проверка пароля для email: {}", request.getEmail());
        boolean isValid = userService.verifyPassword(request.getEmail(), request.getPassword());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Неверный пароль")
            );
        }
        return ResponseEntity.ok(new PasswordVerificationResponse(isValid));
    }

    static class PasswordVerificationRequest {
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        private String email;

        @NotBlank(message = "Пароль не может быть пустым")
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    static class PasswordVerificationResponse {
        private boolean valid;

        public PasswordVerificationResponse(boolean valid) {
            this.valid = valid;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }

    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
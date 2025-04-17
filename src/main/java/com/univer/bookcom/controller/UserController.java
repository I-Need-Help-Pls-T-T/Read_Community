package com.univer.bookcom.controller;

import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.UserRepository;
import com.univer.bookcom.service.BookService;
import com.univer.bookcom.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UserController {
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

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id).orElseThrow(() ->
                            new UserNotFoundException("Пользователь с id " + id + bookNot));

            return ResponseEntity.ok(user);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            User author = userService.getUserById(id).orElseThrow(() ->
                            new UserNotFoundException("Пользователь с id " + id + bookNot));

            List<Book> books = bookService.findBooksByAuthor(author);

            for (Book book : books) {
                book.removeAuthor(author);

                if (book.getAuthors().isEmpty()) {
                    bookService.deleteBook(book.getId());
                } else {
                    bookService.saveBook(book);
                }
            }

            userService.deleteUser(id);

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<Object> searchUsersByName(@RequestParam String name) {
        try {
            List<User> users = userService.findUsersByName(name);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Пользователи с именем '" + name + bookNot);
            }

            return ResponseEntity.ok(users);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при поиске пользователей: " + e.getMessage());
        }
    }

    @GetMapping("/search/email")
    public ResponseEntity<?> searchUserByEmail(@RequestParam String email) {
        try {
            User user = userService.findUserByEmail(email).orElseThrow(() ->
                            new UserNotFoundException("Пользователь с email " + email + bookNot));
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователи с почтой " + email + bookNot);
        }
    }

    @PostMapping("/{userId}/books")
    public ResponseEntity<Void> addBookToUser(
            @PathVariable Long userId,
            @RequestBody Book book) {
        try {
            userService.addBookToUser(userId, book);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{userId}/books/{bookId}")
    public ResponseEntity<Void> removeBookFromUser(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        try {
            Book book = new Book();
            book.setId(bookId);
            userService.removeBookFromUser(userId, book);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/by-book-title")
    public ResponseEntity<?> getUsersByBookTitle(@RequestParam String title) {
        try {
            List<User> users = userRepository.findUsersByBookTitle(title);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Книга с названием '" + title + "' не найдена или у неё нет авторов");
            }

            return ResponseEntity.ok(users);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла внутренняя ошибка сервера");
        }
    }

    // JPQL запрос
    @GetMapping("/search/by-book-title")
    public ResponseEntity<?> getAuthorsByBookTitle(@RequestParam String title) {
        try {
            List<User> authors = userRepository.findAuthorsByBookTitle(title);

            if (authors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Авторы книги с названием '" + title + "' не найдены");
            }

            return ResponseEntity.ok(authors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при поиске авторов: " + e.getMessage());
        }
    }

    // Native SQL запрос
    @GetMapping("/search/by-book-title/native")
    public ResponseEntity<?> getAuthorsByBookTitleNative(@RequestParam String title) {
        try {
            List<User> authors = userRepository.findAuthorsByBookTitleNative(title);

            if (authors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Авторы книги с названием '" + title + "' не найдены");
            }

            return ResponseEntity.ok(authors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при поиске авторов: " + e.getMessage());
        }
    }
}
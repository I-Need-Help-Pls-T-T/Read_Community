package com.univer.bookcom.controller;

import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
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
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BookNotFoundException("Книга с id " + id + " не найдена"));
    }

    @PostMapping("/{authorId}/book")
    public ResponseEntity<Book> createBookWithAuthor(
            @PathVariable Long authorId,
            @RequestBody Book book) {
        try {
            Book savedBook = bookService.createBookWithAuthor(authorId, book);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        try {
            Book book = bookService.updateBook(id, updatedBook);
            return ResponseEntity.ok(book);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam String title) {
        List<Book> books = bookService.findBooksByTitle(title);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@RequestParam String author) {
        List<Book> books = bookService.findBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/year")
    public ResponseEntity<List<Book>> searchBooksByYear(@RequestParam Long year) {
        List<Book> books = bookService.findBooksByPublicYear(year);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/status")
    public ResponseEntity<List<Book>> searchBooksByStatus(@RequestParam BookStatus bookStatus) {
        List<Book> books = bookService.findBooksByStatus(bookStatus);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> addAuthorToBook(
            @PathVariable Long bookId,
            @PathVariable Long authorId) {
        try {
            Book book = bookService.getBookById(bookId).orElseThrow(() ->
                    new BookNotFoundException("Книга с id " + bookId + " не найдена"));
            User author = userService.getUserById(authorId).orElseThrow(() ->
                    new UserNotFoundException("Автор с id " + authorId + " не найден"));
            book.addAuthor(author);
            bookService.saveBook(book);
            return ResponseEntity.noContent().build();
        } catch (BookNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> removeAuthorFromBook(
            @PathVariable Long bookId,
            @PathVariable Long authorId) {
        try {
            Book book = bookService.getBookById(bookId).orElseThrow(() ->
                    new BookNotFoundException("Книга с id " + bookId + " не найдена"));

            User author = userService.getUserById(authorId).orElseThrow(() ->
                    new UserNotFoundException("Автор с id " + authorId + " не найден"));

            book.removeAuthor(author);

            if (book.getAuthors().isEmpty()) {
                bookService.deleteBook(bookId);
            } else {
                bookService.saveBook(book);
            }

            return ResponseEntity.noContent().build();
        } catch (BookNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
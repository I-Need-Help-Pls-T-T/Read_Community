package com.univer.bookcom.service;

import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final UserService userService;

    public BookService(BookRepository bookRepository, UserService userService) {
        this.bookRepository = bookRepository;
        this.userService = userService;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return Optional.ofNullable(bookRepository.findById(id).orElseThrow(() -> new
                EntityNotFoundException("Книга не найдена")));
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book updatedBook) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book bookToUpdate = existingBook.get();
            bookToUpdate.setTitle(updatedBook.getTitle());
            bookToUpdate.setCountChapters(updatedBook.getCountChapters());
            bookToUpdate.setPublicYear(updatedBook.getPublicYear());
            bookToUpdate.setBookStatus(updatedBook.getBookStatus());
            return bookRepository.save(bookToUpdate);
        } else {
            throw new BookNotFoundException("Книга с этим id не найдена: " + id);
        }
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Book> findBooksByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public List<Book> findBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    public List<Book> findBooksByPublicYear(long publicYear) {
        return bookRepository.findByPublicYear(publicYear);
    }

    public List<Book> findBooksByStatus(BookStatus bookStatus) {
        return bookRepository.findByStatus(bookStatus);
    }

    public void removeAuthorFromBook(Long bookId, Long authorId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new BookNotFoundException("Книга с id " + bookId + " не найдена"));
        User author = book.getAuthors().stream()
                .filter(a -> a.getId().equals(authorId))
                .findFirst().orElseThrow(() ->
                        new UserNotFoundException("Автор с id " + authorId + " не найден"));

        book.removeAuthor(author);
        if (book.getAuthors().isEmpty()) {
            bookRepository.delete(book);
        } else {
            bookRepository.save(book);
        }
    }

    public Book createBookWithAuthor(Long authorId, Book book) {
        User author = userService.getUserById(authorId).orElseThrow(() ->
                new UserNotFoundException("Автор с id " + authorId + " не найден"));

        Book savedBook = bookRepository.save(book);

        savedBook.addAuthor(author);

        return bookRepository.save(savedBook);
    }

    public List<Book> findBooksByAuthor(User author) {
        return bookRepository.findByAuthorsContaining(author);
    }
}
package com.univer.bookcom.service;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    @Autowired
    private final BookRepository bookRepository;
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(long id) {
        return Optional.ofNullable(bookRepository.findById(id).orElseThrow(() -> new
                EntityNotFoundException("Книга не найдена")));
    }

    public Book saveBook(Book book) {
        // добавить проверку
        return bookRepository.save(book);
    }

    public Book updateBook(long id, Book updatedBook) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book bookToUpdate = existingBook.get();
            bookToUpdate.setTitle(updatedBook.getTitle());
            bookToUpdate.setAuthor(updatedBook.getAuthor());
            bookToUpdate.setCountChapters(updatedBook.getCountChapters());
            bookToUpdate.setPublicYear(updatedBook.getPublicYear());
            bookToUpdate.setBookStatus(updatedBook.getBookStatus());
            return bookRepository.save(bookToUpdate);
        } else {
            throw new RuntimeException("Книга с этим id не найдена: " + id);
        }
    }

    public void deleteBook(long id) {
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

    public List<Book> findBooksByCountChaptersGreaterThanEqual(long countChapters) {
        return bookRepository.findByCountChaptersGreaterThanEqual(countChapters);
    }

    public List<Book> findBooksByStatus(BookStatus status) {
        return bookRepository.findByBookStatus(status);
    }

    @Transactional
    public void addTranslatorToBook(long bookId, long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Книга с этим id не найдена: " + bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Пользователь с этим id не найден: " + userId));

        book.getTranslators().add(user);
        user.getBooks().add(book);

        bookRepository.save(book);
        userRepository.save(user);
    }

    @Transactional
    public void removeTranslatorFromBook(long bookId, long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Книга с этим id не найдена: " + bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Пользователь с этим id не найден: " + userId));

        book.getTranslators().remove(user);
        user.getBooks().remove(book);

        bookRepository.save(book);
        userRepository.save(user);
    }
}
package com.univer.bookcom.service;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
}
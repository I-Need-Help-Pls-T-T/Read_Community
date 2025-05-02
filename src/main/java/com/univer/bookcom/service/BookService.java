package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final UserService userService;
    private final Map<Long, CacheEntry<Book>> bookCache;
    private static final int MAX_CACHE_SIZE = 3;

    public BookService(BookRepository bookRepository, UserService userService,
                       CacheContainer cacheContainer) {
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.bookCache = cacheContainer.getBookCache(); // получаем кэш книг
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        CacheEntry<Book> cachedEntry = bookCache.get(id);
        if (cachedEntry != null) {
            System.out.println("Книга получена из кэша: " + id);
            return Optional.of(cachedEntry.getValue());
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Книга не найдена"));

        if (bookCache.size() >= MAX_CACHE_SIZE) {
            Long oldestKey = Collections.min(bookCache.entrySet(),
                            Comparator.comparingLong(e -> e.getValue().getTimestamp()))
                    .getKey();
            bookCache.remove(oldestKey);
            System.out.println("Удалена самая старая книга из кэша: " + oldestKey);
        }

        bookCache.put(id, new CacheEntry<>(book));
        System.out.println("Книга добавлена в кэш: " + id);
        return Optional.of(book);
    }

    public Book saveBook(Book book) {
        Book saved = bookRepository.save(book);
        bookCache.put(saved.getId(), new CacheEntry<>(saved));
        System.out.println("Книга сохранена и добавлена в кэш: " + saved.getId());
        return saved;
    }

    public Book updateBook(Long id, Book updatedBook) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book bookToUpdate = existingBook.get();
            bookToUpdate.setTitle(updatedBook.getTitle());
            bookToUpdate.setCountChapters(updatedBook.getCountChapters());
            bookToUpdate.setPublicYear(updatedBook.getPublicYear());
            bookToUpdate.setBookStatus(updatedBook.getBookStatus());
            Book saved = bookRepository.save(bookToUpdate);
            bookCache.put(id, new CacheEntry<>(saved));
            System.out.println("Книга обновлена и добавлена в кэш: " + id);
            return saved;
        } else {
            throw new BookNotFoundException("Книга с этим id не найдена: " + id);
        }
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        bookCache.remove(id);
        System.out.println("Книга удалена из базы и кэша: " + id);
    }

    public List<Book> findBooksByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public List<Book> findBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    public List<Book> findBooksByAuthor(User author) {
        return bookRepository.findByAuthorsContaining(author);
    }

    public List<Book> findBooksByPublicYear(long publicYear) {
        return bookRepository.findByPublicYear(publicYear);
    }

    public List<Book> findBooksByStatus(BookStatus bookStatus) {
        return bookRepository.findByStatus(bookStatus);
    }

    public Book createBookWithAuthor(Long authorId, Book book) {
        User author = userService.getUserById(authorId).orElseThrow(() ->
                new UserNotFoundException("Автор с id " + authorId + " не найден"));

        Book savedBook = bookRepository.save(book);
        savedBook.addAuthor(author);

        Book finalSaved = bookRepository.save(savedBook);
        bookCache.put(finalSaved.getId(), new CacheEntry<>(finalSaved));
        System.out.println("Книга с автором создана и добавлена в кэш: " + finalSaved.getId());

        return finalSaved;
    }

    public boolean isCachedOrExists(Long id) {
        return bookCache.containsKey(id) || bookRepository.existsById(id);
    }
}
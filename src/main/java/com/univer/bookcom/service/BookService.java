package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private static final int MAX_CACHE_SIZE = 3;

    private final BookRepository bookRepository;
    private final UserService userService;
    private final Map<Long, CacheEntry<Book>> bookCache;

    public BookService(BookRepository bookRepository, UserService userService,
                       CacheContainer cacheContainer) {
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.bookCache = cacheContainer.getBookCache();
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        CacheEntry<Book> cacheEntry = bookCache.get(id);
        if (cacheEntry != null) {
            log.debug("Книга найдена в кэше: {}", id);
            return Optional.of(cacheEntry.getValue());
        }

        return bookRepository.findById(id).map(book -> {
            addToCache(id, book);
            return book;
        });
    }

    @Transactional
    public Book saveBook(Book book) {
        Book saved = bookRepository.save(book);
        addToCache(saved.getId(), saved);
        log.info("Книга сохранена и закэширована: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Book updateBook(Long id, Book updatedBook) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Книга с этим id не найдена: " + id));

        existing.setTitle(updatedBook.getTitle());
        existing.setCountChapters(updatedBook.getCountChapters());
        existing.setPublicYear(updatedBook.getPublicYear());
        existing.setBookStatus(updatedBook.getBookStatus());

        Book saved = bookRepository.save(existing);
        addToCache(id, saved);
        log.info("Книга обновлена и закэширована: {}", id);
        return saved;
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        bookCache.remove(id);
        log.info("Книга удалена из базы и кэша: {}", id);
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

    @Transactional
    public Book createBookWithAuthor(Long authorId, Book book) {
        User author = userService.getUserById(authorId)
                .orElseThrow(() ->
                        new UserNotFoundException("Автор с id " + authorId + " не найден"));

        Book saved = bookRepository.save(book);
        saved.addAuthor(author);

        Book finalSaved = bookRepository.save(saved);
        addToCache(finalSaved.getId(), finalSaved);
        log.info("Книга с автором создана и закэширована: {}", finalSaved.getId());

        return finalSaved;
    }

    public boolean isCachedOrExists(Long id) {
        return bookCache.containsKey(id) || bookRepository.existsById(id);
    }

    private void addToCache(Long id, Book book) {
        if (bookCache.size() >= MAX_CACHE_SIZE) {
            Optional<Map.Entry<Long, CacheEntry<Book>>> oldestEntry = bookCache.entrySet().stream()
                    .min(Comparator.comparingLong(e -> e.getValue().getTimestamp()));
            oldestEntry.ifPresent(entry -> {
                bookCache.remove(entry.getKey());
                log.debug("Удалена устаревшая запись из кэша: {}", entry.getKey());
            });
        }
        bookCache.put(id, new CacheEntry<>(book));
        log.debug("Книга добавлена в кэш: {}", id);
    }
}
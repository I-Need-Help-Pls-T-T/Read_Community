package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.dto.request.BookRequestDto;
import com.univer.bookcom.dto.response.BookResponseDto;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.mapper.DtoMapper;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private static final int MAX_CACHE_SIZE = 3;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final Map<Long, CacheEntry<Book>> bookCache;
    private final DtoMapper dtoMapper;

    public BookService(BookRepository bookRepository, UserRepository userRepository,
                       CacheContainer cacheContainer, DtoMapper dtoMapper) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookCache = cacheContainer.getBookCache();
        this.dtoMapper = dtoMapper;
    }

    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<BookResponseDto> getBookById(Long id) {
        CacheEntry<Book> cacheEntry = bookCache.get(id);
        if (cacheEntry != null) {
            log.debug("Книга найдена в кэше: {}", id);
            return Optional.of(dtoMapper.toBookResponseDto(cacheEntry.getValue()));
        }

        return bookRepository.findById(id)
                .map(book -> {
                    addToCache(id, book);
                    return dtoMapper.toBookResponseDto(book);
                });
    }

    @Transactional
    public BookResponseDto saveBook(BookRequestDto bookDto) {
        Book book = dtoMapper.toBookEntity(bookDto);
        Book saved = bookRepository.save(book);
        addToCache(saved.getId(), saved);
        log.info("Книга сохранена и закэширована: {}", saved.getId());
        return dtoMapper.toBookResponseDto(saved);
    }

    @Transactional
    public BookResponseDto updateBook(Long id, BookRequestDto bookDto) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Книга с этим id не найдена: " + id));

        existing.setTitle(bookDto.getTitle());
        existing.setCountChapters(bookDto.getCountChapters());
        existing.setPublicYear(bookDto.getPublicYear());
        existing.setBookStatus(bookDto.getStatus());

        if (bookDto.getAuthorIds() != null) {
            List<User> authors = bookDto.getAuthorIds().stream()
                    .map(authorId -> userRepository.findById(authorId)
                            .orElseThrow(() -> new UserNotFoundException("Автор с id " + authorId + " не найден")))
                    .collect(Collectors.toList());
            existing.setAuthors(authors);
        }

        Book saved = bookRepository.save(existing);
        addToCache(id, saved);
        log.info("Книга обновлена и закэширована: {}", id);
        return dtoMapper.toBookResponseDto(saved);
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        bookCache.remove(id);
        log.info("Книга удалена из базы и кэша: {}", id);
    }

    public List<BookResponseDto> findBooksByTitle(String title) {
        return bookRepository.findByTitle(title).stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> findBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author).stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> findBooksByAuthor(User author) {
        return bookRepository.findByAuthorsContaining(author).stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> findBooksByPublicYear(long publicYear) {
        return bookRepository.findByPublicYear(publicYear).stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> findBooksByStatus(BookStatus bookStatus) {
        return bookRepository.findByStatus(bookStatus).stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookResponseDto createBookWithAuthor(Long authorId, BookRequestDto bookDto) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException("Автор с id " + authorId + " не найден"));

        Book book = dtoMapper.toBookEntity(bookDto);
        Book saved = bookRepository.save(book);
        saved.addAuthor(author);

        Book finalSaved = bookRepository.save(saved);
        addToCache(finalSaved.getId(), finalSaved);
        log.info("Книга с автором создана и закэширована: {}", finalSaved.getId());

        return dtoMapper.toBookResponseDto(finalSaved);
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
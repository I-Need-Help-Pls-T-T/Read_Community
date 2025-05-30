package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.model.dto.request.BookRequestDto;
import com.univer.bookcom.model.dto.response.BookResponseDto;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import com.univer.bookcom.service.mapper.BookMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private static final int MAX_CACHE_SIZE = 3;

    private final BookRepository bookRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;
    private final BookMapper bookMapper;
    private final Map<Long, CacheEntry<Book>> bookCache;
    private final CacheContainer cacheContainer;

    public BookService(BookRepository bookRepository, UserService userService,
                       UserRepository userRepository, CommentsRepository commentsRepository,
                       BookMapper bookMapper, CacheContainer cacheContainer) {
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.commentsRepository = commentsRepository;
        this.bookMapper = bookMapper;
        this.bookCache = cacheContainer.getBookCache();
        this.cacheContainer = cacheContainer;
    }

    @Transactional
    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<BookResponseDto> getBookById(Long id) {
        CacheEntry<Book> cacheEntry = bookCache.get(id);
        if (cacheEntry != null) {
            log.debug("Книга найдена в кэше: {}", id);
            Book book = cacheEntry.getValue();
            Hibernate.initialize(book.getAuthors());
            log.debug("Коллекция authors для кэшированной книги ID {} инициализирована: {}",
                    id, Hibernate.isInitialized(book.getAuthors()));
            return Optional.of(bookMapper.toResponseDto(book));
        }

        return bookRepository.findById(id)
                .map(book -> {
                    Hibernate.initialize(book.getAuthors());
                    log.debug("Коллекция authors для книги ID {} из БД инициализирована: {}",
                            id, Hibernate.isInitialized(book.getAuthors()));
                    addToCache(id, book);
                    return bookMapper.toResponseDto(book);
                });
    }

    @Transactional
    public BookResponseDto updateBook(Long id, BookRequestDto updatedBookDto) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Книга с id " + id + " не найдена"));

        existing.setTitle(updatedBookDto.getTitle());
        existing.setCountChapters(updatedBookDto.getCountChapters());
        existing.setPublicYear(updatedBookDto.getPublicYear());
        existing.setDescription(updatedBookDto.getDescription());
        existing.setBookStatus(BookStatus.valueOf(updatedBookDto.getBookStatus()));

        Hibernate.initialize(existing.getAuthors());
        log.debug("Коллекция authors для книги ID {} инициализирована перед обновлением: {}",
                id, Hibernate.isInitialized(existing.getAuthors()));
        Book saved = bookRepository.save(existing);
        addToCache(id, saved);
        log.info("Книга обновлена и закэширована: {}", id);
        return bookMapper.toResponseDto(saved);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Книга с id " + id + " не найдена"));

        Hibernate.initialize(book.getAuthors());
        Hibernate.initialize(book.getComments());

        log.debug("Книга ID {}: связанные авторы={}", id,
                book.getAuthors().stream().map(User::getId).collect(Collectors.toList()));

        List<User> authors = new ArrayList<>(book.getAuthors());
        for (User author : authors) {
            Hibernate.initialize(author.getBooks());
            Hibernate.initialize(author.getComments());
            author.getBooks().removeIf(b -> b.getId().equals(id));
            author.getComments().removeIf(comment ->
                    !commentsRepository.existsById(comment.getId()));
            userRepository.save(author);
            cacheContainer.getUserCache().put(author.getId(), new CacheEntry<>(author));
            log.debug("Книга ID {} удалена из коллекции books пользователя ID {}",
                    id, author.getId());
        }

        book.getComments().forEach(comment -> {
            commentsRepository.delete(comment);
            cacheContainer.getCommentsCache().remove(comment.getId());
            log.debug("Комментарий ID {} удалён для книги ID {}", comment.getId(), id);
        });

        bookRepository.delete(book);
        bookCache.remove(id);
        log.info("Книга удалена из базы и кэша: {}", id);
    }

    @Transactional
    public List<BookResponseDto> findBooksByTitle(String title) {
        return bookRepository.findByTitle(title).stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookResponseDto> findBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author).stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookResponseDto> findBooksByPublicYear(long publicYear) {
        return bookRepository.findByPublicYear(publicYear).stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookResponseDto> findBooksByStatus(String status) {
        return bookRepository.findByStatus(BookStatus.valueOf(status)).stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookResponseDto createBookWithAuthor(Long authorId, BookRequestDto bookRequestDto) {
        log.debug("Создание книги с authorId={}", authorId);

        User author = userService.getUserById(authorId)
                .orElseThrow(() -> {
                    log.error("Автор с id {} не найден", authorId);
                    return new UserNotFoundException("Автор с id " + authorId + " не найден");
                });

        Hibernate.initialize(author.getBooks());
        final List<Long> bookIds = author.getBooks().stream().map(Book::getId)
                .collect(Collectors.toList());
        author.getBooks().removeIf(book -> !bookRepository.existsById(book.getId()));
        userRepository.save(author);
        cacheContainer.getUserCache().put(authorId, new CacheEntry<>(author));
        log.debug("Устаревшие книги удалены из коллекции books пользователя ID {}."
                        + "Оставшиеся книги: {}",
                authorId, bookIds);

        Book book = bookMapper.toEntity(bookRequestDto);
        Hibernate.initialize(book.getAuthors());
        log.debug("Коллекция authors для новой книги инициализирована: {}",
                Hibernate.isInitialized(book.getAuthors()));
        Book saved = bookRepository.save(book);

        if (!bookRepository.existsByIdAndAuthorId(saved.getId(), authorId)) {
            saved.addAuthor(author);
            Hibernate.initialize(saved.getAuthors());
            log.debug("Коллекция authors для книги ID {} инициализирована после добавления автора:"
                    + "{}", saved.getId(), Hibernate.isInitialized(saved.getAuthors()));
            saved = bookRepository.save(saved);
        }

        addToCache(saved.getId(), saved);
        log.info("Книга с автором создана: {}", saved.getId());
        return bookMapper.toResponseDto(saved);
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
                log.debug("Удалена устаревшая книга из кэша: {}", entry.getKey());
            });
        }
        Hibernate.initialize(book.getAuthors());
        log.debug("Коллекция authors для книги ID {} инициализирована: {}",
                id, Hibernate.isInitialized(book.getAuthors()));
        bookCache.put(id, new CacheEntry<>(book));
        log.debug("Добавляем книгу в кэш: {}", id);
    }

    @Transactional
    public void addAuthorToBook(Long bookId, Long authorId) {
        log.debug("Попытка добавить автора с ID {} к книге ID {}", authorId, bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Книга с id {} не найдена", bookId);
                    return new BookNotFoundException("Книга с id " + bookId + " не найдена");
                });
        User author = userService.getUserById(authorId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", authorId);
                    return new UserNotFoundException("Пользователь с id "
                            + authorId + " не найден");
                });

        if (bookRepository.existsByIdAndAuthorId(bookId, authorId)) {
            log.warn("Пользователь с ID {} уже является автором ID {}", authorId, bookId);
            return;
        }

        try {
            book.addAuthor(author);
            Hibernate.initialize(book.getAuthors());
            log.debug("Коллекция authors для книги ID {} инициализирована перед добавлением"
                    + "автора: {}", bookId, Hibernate.isInitialized(book.getAuthors()));
            Book saved = bookRepository.save(book);
            addToCache(saved.getId(), saved);
            log.info("Пользователь с ID {} успешно добавлен к книге ID {}", authorId, bookId);
        } catch (Exception e) {
            log.error("Ошибка при добавлении пользователя с ID {} к книге ID {}: {}",
                    authorId, bookId, e.getMessage());
            throw new RuntimeException("Не удалось добавить пользователя к книге: "
                    + e.getMessage(), e);
        }
    }

    @Transactional
    public void removeAuthorFromBook(Long bookId, Long authorId) {
        log.debug("Попытка удалить автора с ID {} из книги с ID {}", authorId, bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Книга с id {} не найдена", bookId);
                    return new BookNotFoundException("Книга с id " + bookId + " не найдена");
                });
        User author = userService.getUserById(authorId)
                .orElseThrow(() -> {
                    log.error("Автор с id {} не найден", authorId);
                    return new UserNotFoundException("Автор с id " + authorId + " не найден");
                });

        if (!bookRepository.existsByIdAndAuthorId(bookId, authorId)) {
            log.warn("Автор с ID {} не связан с книгой с ID {}", authorId, bookId);
            return;
        }

        try {
            Hibernate.initialize(book.getAuthors());

            log.debug("Авторы книги ID {} до удаления: {}",
                    bookId,
                    book.getAuthors().stream().map(User::getId).collect(Collectors.toList()));

            book.getAuthors().removeIf(a -> a.getId().equals(authorId));

            Hibernate.initialize(author.getBooks());
            author.getBooks().removeIf(b -> b.getId().equals(bookId));
            userRepository.save(author);
            log.debug("Пользователь ID {} сохранен после удаления книги ID {}", authorId, bookId);

            log.debug("Авторы книги ID {} после удаления: {}",
                    bookId,
                    book.getAuthors().stream().map(User::getId).collect(Collectors.toList()));
            log.debug("Книги пользователя ID {} после удаления: {}",
                    authorId,
                    author.getBooks().stream().map(Book::getId).collect(Collectors.toList()));

            bookCache.remove(bookId);
            log.debug("Кэш книги ID {} очищен перед сохранением", bookId);

            bookRepository.save(book);

            Map<Long, CacheEntry<User>> userCache = cacheContainer.getUserCache();
            userCache.remove(authorId);
            log.debug("Кэш пользователя ID {} очищен после удаления книги ID {}", authorId, bookId);

            List<User> associatedUsers = userRepository.findUsersByBookId(bookId);
            if (book.getAuthors().isEmpty() && associatedUsers.isEmpty()) {
                bookRepository.deleteById(bookId);
                bookCache.remove(bookId);
                log.info("Книга с ID {} удалена, так как не осталось авторов"
                        + "или связанных пользователей", bookId);
            } else {
                Hibernate.initialize(book.getAuthors());
                addToCache(book.getId(), book);
                log.info("Автор с ID {} удален из книги с ID {}. Книга сохранена.",
                        authorId, bookId);
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении автора с ID {} из книги с ID {}: {}",
                    authorId, bookId, e.getMessage());
            throw new RuntimeException("Не удалось удалить автора из книги: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<String> getAuthorNamesByBookId(Long bookId) {
        log.debug("Получение авторов для книги с ID {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Книга с id "
                        + bookId + " не найдена"));
        Hibernate.initialize(book.getAuthors());
        log.debug("Коллекция authors для книги ID {} инициализирована: {}",
                bookId, Hibernate.isInitialized(book.getAuthors()));
        return book.getAuthors().stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }
}
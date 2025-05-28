package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.InvalidBookDataException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.model.dto.request.BookRequestDto;
import com.univer.bookcom.model.dto.request.UserRequestDto;
import com.univer.bookcom.model.dto.response.BookResponseDto;
import com.univer.bookcom.model.dto.response.UserResponseDto;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import com.univer.bookcom.service.mapper.BookMapper;
import com.univer.bookcom.service.mapper.UserMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "Пользователь с id %d не найден";

    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;
    private final BookRepository bookRepository;
    private final CacheContainer cacheContainer;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    public UserService(UserRepository userRepository,
                       CommentsRepository commentsRepository,
                       BookRepository bookRepository,
                       CacheContainer cacheContainer,
                       UserMapper userMapper,
                       BookMapper bookMapper) {
        this.userRepository = userRepository;
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.cacheContainer = cacheContainer;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public List<UserResponseDto> getAllUsersDto() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<UserResponseDto> getUserByIdDto(Long id) {
        Map<Long, CacheEntry<User>> cache = cacheContainer.getUserCache();
        if (cache.containsKey(id)) {
            log.debug("Пользователь найден в кэше: id = {}", id);
            User user = cache.get(id).getValue();
            Hibernate.initialize(user.getBooks());
            Hibernate.initialize(user.getComments());

            List<Long> bookIdsBefore = user.getBooks().stream()
                    .map(Book::getId)
                    .collect(Collectors.toList());
            user.getBooks().removeIf(book -> !bookRepository.existsById(book.getId()));
            List<Long> bookIdsAfter = user.getBooks().stream()
                    .map(Book::getId)
                    .collect(Collectors.toList());
            if (!bookIdsBefore.equals(bookIdsAfter)) {
                userRepository.save(user);
                log.debug("Устаревшие книги удалены из коллекции books"
                                + " пользователя ID {}. Было: {}, стало: {}",
                        id, bookIdsBefore, bookIdsAfter);
            }

            log.debug("Коллекция books для пользователя ID {} инициализирована, содержит {} книг",
                    id, user.getBooks().size());
            log.debug("Коллекция comments для пользователя ID {} инициализирована: {}",
                    id, Hibernate.isInitialized(user.getComments()));

            cache.put(id, new CacheEntry<>(user));
            return Optional.of(userMapper.toResponseDto(user));
        }
        return userRepository.findById(id)
                .map(user -> {
                    Hibernate.initialize(user.getBooks());
                    Hibernate.initialize(user.getComments());

                    List<Long> bookIdsBefore = user.getBooks().stream()
                            .map(Book::getId)
                            .collect(Collectors.toList());
                    user.getBooks().removeIf(book -> !bookRepository.existsById(book.getId()));
                    List<Long> bookIdsAfter = user.getBooks().stream()
                            .map(Book::getId)
                            .collect(Collectors.toList());
                    if (!bookIdsBefore.equals(bookIdsAfter)) {
                        userRepository.save(user);
                        log.debug("Устаревшие книги удалены из коллекции books"
                                        + "пользователя ID {}. Было: {}, стало: {}",
                                id, bookIdsBefore, bookIdsAfter);
                    }

                    log.debug("Коллекция books для пользователя ID {} из БД"
                                    + "инициализирована, содержит {} книг",
                            id, user.getBooks().size());
                    log.debug("Коллекция comments для пользователя ID {}"
                                    + "из БД инициализирована: {}",
                            id, Hibernate.isInitialized(user.getComments()));

                    cache.put(user.getId(), new CacheEntry<>(user));
                    return userMapper.toResponseDto(user);
                });
    }

    @Transactional
    public Optional<User> getUserById(Long id) {
        Map<Long, CacheEntry<User>> cache = cacheContainer.getUserCache();
        if (cache.containsKey(id)) {
            log.debug("Пользователь найден в кэше: id = {}", id);
            User user = cache.get(id).getValue();
            Hibernate.initialize(user.getBooks());
            Hibernate.initialize(user.getComments());

            user.getBooks().removeIf(book -> !bookRepository.existsById(book.getId()));
            user.getComments().removeIf(comment -> !commentsRepository.existsById(comment.getId()));
            cache.put(id, new CacheEntry<>(user));
            log.debug("Коллекция comments для пользователя ID {} инициализирована: {}",
                    id, Hibernate.isInitialized(user.getComments()));
            return Optional.of(user);
        }
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> {
            Hibernate.initialize(u.getBooks());
            Hibernate.initialize(u.getComments());

            u.getBooks().removeIf(book -> !bookRepository.existsById(book.getId()));
            u.getComments().removeIf(comment -> !commentsRepository.existsById(comment.getId()));
            cache.put(u.getId(), new CacheEntry<>(u));
            log.debug("Коллекция comments для пользователя ID {} из БД инициализирована: {}",
                    id, Hibernate.isInitialized(u.getComments()));
        });
        return user;
    }

    @Transactional
    public UserResponseDto saveUserDto(UserRequestDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);

        Hibernate.initialize(saved.getBooks());
        Hibernate.initialize(saved.getComments());

        cacheContainer.getUserCache().put(saved.getId(), new CacheEntry<>(saved));
        log.debug("Коллекция comments для пользователя ID {} инициализирована: {}",
                saved.getId(), Hibernate.isInitialized(saved.getComments()));
        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto updateUserDto(Long id, UserRequestDto updatedUserDto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, id)));
        existing.setName(updatedUserDto.getName());
        existing.setEmail(updatedUserDto.getEmail());
        existing.setPassword(updatedUserDto.getPassword());
        User updated = userRepository.save(existing);
        Hibernate.initialize(updated.getBooks());
        Hibernate.initialize(updated.getComments());
        cacheContainer.getUserCache().put(id, new CacheEntry<>(updated));
        log.debug("Коллекция comments для пользователя ID {} инициализирована: {}",
                id, Hibernate.isInitialized(updated.getComments()));
        return userMapper.toResponseDto(updated);
    }

    @Transactional
    public boolean existsById(Long id) {
        Map<Long, CacheEntry<User>> cache = cacheContainer.getUserCache();
        return cache.containsKey(id) || userRepository.existsById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, id)));

        Hibernate.initialize(user.getBooks());
        Hibernate.initialize(user.getComments());

        log.debug("Кэш пользователей перед удалением ID {}: {}",
                id, cacheContainer.getUserCache().keySet());
        log.debug("Кэш книг перед удалением ID {}: {}", id, cacheContainer.getBookCache().keySet());

        commentsRepository.deleteAll(user.getComments());

        List<Book> booksToProcess = new ArrayList<>(user.getBooks());
        for (Book book : booksToProcess) {
            Hibernate.initialize(book.getAuthors());
            log.debug("Книга ID {}: текущие авторы={}",
                    book.getId(),
                    book.getAuthors().stream().map(User::getId).collect(Collectors.toList()));

            book.getAuthors().remove(user);
            user.getBooks().remove(book);
            bookRepository.save(book);

            List<User> associatedUsers = userRepository.findUsersByBookId(book.getId());
            boolean hasOtherAuthors = !book.getAuthors().isEmpty();
            boolean hasOtherUsers = associatedUsers.stream().anyMatch(u -> !u.getId().equals(id));

            log.debug("Книга ID {}: hasOtherAuthors={}, hasOtherUsers={}, associatedUsers={}",
                    book.getId(), hasOtherAuthors, hasOtherUsers,
                    associatedUsers.stream().map(User::getId).collect(Collectors.toList()));

            if (hasOtherAuthors || hasOtherUsers) {
                log.info("Книга с ID {} сохранена: есть другие авторы или связанные пользователи",
                        book.getId());
                bookRepository.save(book);
                cacheContainer.getBookCache().remove(book.getId());
            } else {
                log.info("Книга с ID {} удалена: нет других авторов и связанных пользователей",
                        book.getId());
                bookRepository.delete(book);
                cacheContainer.getBookCache().remove(book.getId());
            }
        }

        userRepository.deleteBookUserAssociations(id);
        userRepository.delete(user);
        cacheContainer.getUserCache().remove(id);

        log.debug("Кэш пользователей после удаления ID {}: {}",
                id, cacheContainer.getUserCache().keySet());
        log.debug("Кэш книг после удаления ID {}: {}", id, cacheContainer.getBookCache().keySet());
    }

    @Transactional
    public List<UserResponseDto> findUsersByNameDto(String name) {
        return userRepository.findByNameContaining(name).stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<UserResponseDto> findUserByEmailDto(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDto);
    }

    @Transactional
    public void addBookToUserDto(Long userId, BookRequestDto bookDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        Book book = bookMapper.toEntity(bookDto);
        Optional<Book> existingBook = bookRepository
                .findByTitleAndCountChaptersAndPublicYearAndStatus(book.getTitle(),
                        book.getCountChapters(), book.getPublicYear(), book.getBookStatus());
        if (existingBook.isPresent()) {
            book = existingBook.get();
        } else {
            book = bookRepository.save(book);
        }
        user.addBook(book);
        User updated = userRepository.save(user);
        Hibernate.initialize(updated.getBooks());
        Hibernate.initialize(updated.getComments());
        cacheContainer.getUserCache().put(updated.getId(), new CacheEntry<>(updated));
        log.debug("Коллекция comments для пользователя ID {} инициализирована: {}",
                updated.getId(), Hibernate.isInitialized(updated.getComments()));
    }

    @Transactional
    public void removeBookFromUserDto(Long userId, Long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new BookNotFoundException("Книга с id " + bookId + " не найдена"));

        Hibernate.initialize(user.getBooks());
        Hibernate.initialize(user.getComments());
        Hibernate.initialize(book.getAuthors());

        log.debug("Книги пользователя ID {} до удаления: {}", userId,
                user.getBooks().stream().map(Book::getId).collect(Collectors.toList()));
        log.debug("Авторы книги ID {} до удаления: {}", bookId,
                book.getAuthors().stream().map(User::getId).collect(Collectors.toList()));

        user.removeBook(book);
        User updatedUser = userRepository.save(user);

        boolean deleteBook = book.getAuthors().isEmpty();
        if (deleteBook) {
            bookRepository.delete(book);
            cacheContainer.getBookCache().remove(bookId);
            log.info("Книга ID {} удалена, так как не осталось авторов", bookId);
        } else {
            Book updatedBook = bookRepository.save(book);
            Hibernate.initialize(updatedBook.getAuthors());
            Hibernate.initialize(updatedBook.getComments());
            cacheContainer.getBookCache().put(bookId, new CacheEntry<>(updatedBook));
            log.debug("Книга ID {} сохранена, кэш обновлён", bookId);
        }

        updatedUser.getBooks().removeIf(b -> !bookRepository.existsById(b.getId()));

        Hibernate.initialize(updatedUser.getBooks());
        Hibernate.initialize(updatedUser.getComments());

        log.debug("Книги пользователя ID {} после удаления: {}", userId,
                updatedUser.getBooks().stream().map(Book::getId).collect(Collectors.toList()));

        cacheContainer.getUserCache().put(userId, new CacheEntry<>(updatedUser));
        log.info("Пользователь ID {} обновлён, книга ID {} удалена из списка", userId, bookId);
    }

    @Transactional
    public List<UserResponseDto> findUsersByBookTitleDto(String title) {
        return userRepository.findUsersByBookTitle(title).stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<UserResponseDto> findAuthorsByBookTitleDto(String title) {
        return userRepository.findAuthorsByBookTitle(title).stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookResponseDto> addBooksToUserBulkDto(Long userId, List<BookRequestDto> bookDtos) {
        log.debug("Начало добавления книг пользователю с ID: {}", userId);
        if (bookDtos == null || bookDtos.isEmpty()) {
            return Collections.emptyList();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new UserNotFoundException(String.format(USER_NOT_FOUND, userId));
                });
        List<BookResponseDto> added = new ArrayList<>();
        for (BookRequestDto bookDto : bookDtos) {
            log.debug("Обработка книги: {}", bookDto.getTitle());
            if (bookDto.getTitle() == null || bookDto.getTitle().trim().isEmpty()) {
                throw new InvalidBookDataException("Название книги не может быть пустым");
            }
            if (bookDto.getCountChapters() < 0) {
                throw new InvalidBookDataException("Количество глав не может быть отрицательным");
            }
            if (bookDto.getPublicYear() < 0) {
                throw new InvalidBookDataException("Год публикации не может быть отрицательным");
            }
            if (bookDto.getBookStatus() == null) {
                throw new InvalidBookDataException("Статус книги не может быть null");
            }
            Book book = bookMapper.toEntity(bookDto);
            boolean isDuplicate = false;
            for (Book existingBook : user.getBooks()) {
                if (book.getTitle().equals(existingBook.getTitle())
                        && book.getCountChapters() == existingBook.getCountChapters()
                        && book.getPublicYear() == existingBook.getPublicYear()
                        && book.getBookStatus() == existingBook.getBookStatus()) {
                    isDuplicate = true;
                    log.info("Пропущен дубликат книги (у пользователя): {}", book.getTitle());
                    break;
                }
            }
            if (isDuplicate) {
                continue;
            }
            Optional<Book> bookInDbOpt = bookRepository
                    .findByTitleAndCountChaptersAndPublicYearAndStatus(book.getTitle(),
                            book.getCountChapters(), book.getPublicYear(), book.getBookStatus());
            if (bookInDbOpt.isPresent()) {
                Book bookInDb = bookInDbOpt.get();
                if (!bookInDb.getAuthors().contains(user)) {
                    log.info("Пропущен дубликат книги (у других авторов): {}", book.getTitle());
                    continue;
                } else {
                    log.debug("Книга с таким названием уже есть у пользователя,"
                            + "добавляем автора: {}", book.getTitle());
                    book = bookInDb;
                }
            } else {
                log.debug("Сохраняем новую книгу в базу: {}", book.getTitle());
                book = bookRepository.save(book);
            }
            book.addAuthor(user);
            added.add(bookMapper.toResponseDto(book));
            log.info("Книга успешно добавлена: {}", book.getTitle());
        }
        userRepository.save(user);
        Hibernate.initialize(user.getBooks());
        Hibernate.initialize(user.getComments());
        cacheContainer.getUserCache().put(user.getId(), new CacheEntry<>(user));
        log.debug("Коллекция comments для пользователя ID {} инициализирована: {}",
                user.getId(), Hibernate.isInitialized(user.getComments()));
        log.debug("Завершено добавление книг пользователю с ID: {}", userId);
        return added;
    }

    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void addBookToUser(Long userId, Book book) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        user.addBook(book);
        User updated = userRepository.save(user);
        cacheContainer.getUserCache().put(updated.getId(), new CacheEntry<>(updated));
    }
}
package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.dto.request.BookRequestDto;
import com.univer.bookcom.dto.request.UserRequestDto;
import com.univer.bookcom.dto.response.BookResponseDto;
import com.univer.bookcom.dto.response.UserResponseDto;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.InvalidBookDataException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.mapper.DtoMapper;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "Пользователь с id %d не найден";

    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;
    private final BookRepository bookRepository;
    private final CacheContainer cacheContainer;
    private final DtoMapper dtoMapper;

    public UserService(UserRepository userRepository,
                       CommentsRepository commentsRepository,
                       BookRepository bookRepository,
                       CacheContainer cacheContainer,
                       DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.cacheContainer = cacheContainer;
        this.dtoMapper = dtoMapper;
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(dtoMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDto> getUserById(Long id) {
        Map<Long, CacheEntry<User>> cache = cacheContainer.getUserCache();
        if (cache.containsKey(id)) {
            log.debug("Пользователь найден в кэше: id = {}", id);
            return Optional.of(dtoMapper.toUserResponseDto(cache.get(id).getValue()));
        }

        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> cache.put(u.getId(), new CacheEntry<>(u)));
        return user.map(dtoMapper::toUserResponseDto);
    }

    public UserResponseDto saveUser(UserRequestDto userDto) {
        User user = dtoMapper.toUserEntity(userDto);
        User saved = userRepository.save(user);
        cacheContainer.getUserCache().put(saved.getId(), new CacheEntry<>(saved));
        return dtoMapper.toUserResponseDto(saved);
    }

    public UserResponseDto updateUser(Long id, UserRequestDto userDto) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDto.getName());
                    user.setEmail(userDto.getEmail());
                    user.setPassword(userDto.getPassword());
                    User updated = userRepository.save(user);
                    cacheContainer.getUserCache().put(id, new CacheEntry<>(updated));
                    return dtoMapper.toUserResponseDto(updated);
                })
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, id)));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, id)));

        commentsRepository.deleteAll(user.getComments());

        user.getBooks().forEach(book -> {
            book.getAuthors().remove(user);
            if (book.getAuthors().isEmpty()) {
                bookRepository.delete(book);
            } else {
                bookRepository.save(book);
            }
        });

        userRepository.delete(user);
        cacheContainer.getUserCache().remove(id);
        log.debug("Удаление пользователя из кэша: id = {}", id);
    }

    public List<UserResponseDto> findUsersByName(String name) {
        return userRepository.findByNameContaining(name).stream()
                .map(dtoMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDto> findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(dtoMapper::toUserResponseDto);
    }

    public void addBookToUser(Long userId, BookRequestDto bookDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        Book book = dtoMapper.toBookEntity(bookDto);
        user.addBook(book);
        User updated = userRepository.save(user);
        cacheContainer.getUserCache().put(updated.getId(), new CacheEntry<>(updated));
    }

    public void removeBookFromUser(Long userId, Long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new BookNotFoundException("Книга с id " + bookId + " не найдена"));
        user.removeBook(book);
        User updated = userRepository.save(user);
        cacheContainer.getUserCache().put(updated.getId(), new CacheEntry<>(updated));
    }

    @Transactional
    public List<BookResponseDto> addBooksToUserBulk(Long userId, List<BookRequestDto> bookDtos) {
        log.debug("Начало добавления книг пользователю с ID: {}", userId);

        if (bookDtos == null || bookDtos.isEmpty()) {
            return Collections.emptyList();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с ID {} не найден", userId);
            return new UserNotFoundException(String.format(USER_NOT_FOUND, userId));
        });

        List<Book> added = new ArrayList<>();

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
            if (bookDto.getStatus() == null) {
                throw new InvalidBookDataException("Статус книги не может быть null");
            }

            boolean isDuplicate = false;

            for (Book existingBook : user.getBooks()) {
                if (bookDto.getTitle().equals(existingBook.getTitle())
                        && bookDto.getCountChapters() == existingBook.getCountChapters()
                        && bookDto.getPublicYear() == existingBook.getPublicYear()
                        && bookDto.getStatus() == existingBook.getBookStatus()) {
                    isDuplicate = true;
                    log.info("Пропущен дубликат книги (у пользователя): {}", bookDto.getTitle());
                    break;
                }
            }
            if (isDuplicate) {
                continue;
            }

            Optional<Book> bookInDbOpt = bookRepository.findByTitleAndCountChaptersAndPublicYearAndStatus(
                    bookDto.getTitle(),
                    bookDto.getCountChapters(),
                    bookDto.getPublicYear(),
                    bookDto.getStatus());

            Book bookToAdd;
            if (bookInDbOpt.isPresent()) {
                Book bookInDb = bookInDbOpt.get();
                if (!bookInDb.getAuthors().contains(user)) {
                    log.info("Пропущен дубликат книги (у других авторов): {}", bookDto.getTitle());
                    continue;
                } else {
                    log.debug("Книга с таким названием уже есть у пользователя, добавляем автора: {}", bookDto.getTitle());
                    bookToAdd = bookInDb;
                }
            } else {
                log.debug("Сохраняем новую книгу в базу: {}", bookDto.getTitle());
                bookToAdd = dtoMapper.toBookEntity(bookDto);
                bookToAdd = bookRepository.save(bookToAdd);
            }

            bookToAdd.addAuthor(user);
            added.add(bookToAdd);
            log.info("Книга успешно добавлена: {}", bookToAdd.getTitle());
        }

        userRepository.save(user);
        cacheContainer.getUserCache().put(user.getId(), new CacheEntry<>(user));
        log.debug("Завершено добавление книг пользователю с ID: {}", userId);

        return added.stream()
                .map(dtoMapper::toBookResponseDto)
                .collect(Collectors.toList());
    }
}
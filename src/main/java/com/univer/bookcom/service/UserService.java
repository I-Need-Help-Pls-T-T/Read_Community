package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public UserService(UserRepository userRepository,
                       CommentsRepository commentsRepository,
                       BookRepository bookRepository,
                       CacheContainer cacheContainer) {
        this.userRepository = userRepository;
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.cacheContainer = cacheContainer;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        Map<Long, CacheEntry<User>> cache = cacheContainer.getUserCache();
        if (cache.containsKey(id)) {
            log.debug("Пользователь найден в кэше: id = {}", id);
            return Optional.of(cache.get(id).getValue());
        }

        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> cache.put(u.getId(), new CacheEntry<>(u)));
        return user;
    }

    public User saveUser(User user) {
        User saved = userRepository.save(user);
        cacheContainer.getUserCache().put(saved.getId(), new CacheEntry<>(saved));
        return saved;
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(updatedUser.getPassword());
                    User updated = userRepository.save(user);
                    cacheContainer.getUserCache().put(id, new CacheEntry<>(updated));
                    return updated;
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

    public List<User> findUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void addBookToUser(Long userId, Book book) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        user.addBook(book);
        User updated = userRepository.save(user);
        cacheContainer.getUserCache().put(updated.getId(), new CacheEntry<>(updated));
    }

    public void removeBookFromUser(Long userId, Book book) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, userId)));
        user.removeBook(book);
        User updated = userRepository.save(user);
        cacheContainer.getUserCache().put(updated.getId(), new CacheEntry<>(updated));
    }
}
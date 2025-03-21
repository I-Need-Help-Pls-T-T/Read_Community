package com.univer.bookcom.service;

import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;
    private final BookRepository bookRepository;

    public UserService(UserRepository userRepository, CommentsRepository commentsRepository,
                       BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(updatedUser.getPassword());
                    user.setCountPublic(updatedUser.getCountPublic());
                    user.setCountTranslate(updatedUser.getCountTranslate());
                    return userRepository.save(user);
                }).orElseThrow(() ->
                        new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("Пользователь с id " + id + " не найден"));

        commentsRepository.deleteAll(user.getComments());

        for (Book book : user.getBooks()) {
            book.getAuthors().remove(user);

            if (book.getAuthors().isEmpty()) {
                bookRepository.delete(book);
            } else {
                bookRepository.save(book);
            }
        }

        userRepository.delete(user);
    }

    public List<User> findUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void addBookToUser(Long userId, Book book) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                        new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        user.addBook(book);
        userRepository.save(user);
    }

    public void removeBookFromUser(Long userId, Book book) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                        new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        user.removeBook(book);
        userRepository.save(user);
    }
}
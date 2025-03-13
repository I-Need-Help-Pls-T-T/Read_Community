package com.univer.bookcom.service;

import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        // добавить проверочку
        return userRepository.save(user);
    }

    public User updateUser(long id, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            userToUpdate.setName(updatedUser.getName());
            userToUpdate.setEmail(updatedUser.getEmail());
            userToUpdate.setPassword(updatedUser.getPassword());
            userToUpdate.setCountPublic(updatedUser.getCountPublic());
            userToUpdate.setCountTranslate(updatedUser.getCountTranslate());
            return userRepository.save(userToUpdate);
        } else {
            throw new RuntimeException("Пользователь с этим id не найден: " + id);
        }
    }

    @Transactional
    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new
                        IllegalArgumentException("Пользователь с этим id не найден: " + id));
        userRepository.delete(user);
    }

    public List<User> findUsersByName(String name) {
        return userRepository.findByName(name);
    }

    public List<User> findUsersByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findUsersByCountPublicGreaterThanEqual(long countPublic) {
        return userRepository.findByCountPublicGreaterThanEqual(countPublic);
    }

    public List<User> findUsersByCountTranslateGreaterThanEqual(long countTranslate) {
        return userRepository.findByCountTranslateGreaterThanEqual(countTranslate);
    }
}
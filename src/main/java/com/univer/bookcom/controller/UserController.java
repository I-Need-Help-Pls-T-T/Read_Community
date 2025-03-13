package com.univer.bookcom.controller;

import com.univer.bookcom.model.User;
import com.univer.bookcom.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable long id, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/search/name")
    public List<User> findUsersByName(@RequestParam String name) {
        return userService.findUsersByName(name);
    }

    @GetMapping("/search/email")
    public List<User> findUsersByEmail(@RequestParam String email) {
        return userService.findUsersByEmail(email);
    }

    @GetMapping("/search/count-public")
    public List<User> findUsersByCountPublicGreaterThanEqual(@RequestParam long countPublic) {
        return userService.findUsersByCountPublicGreaterThanEqual(countPublic);
    }

    @GetMapping("/search/count-translate")
    public List<User> findUsersByCountTranslateGreaterThanEqual(@RequestParam long countTranslate) {
        return userService.findUsersByCountTranslateGreaterThanEqual(countTranslate);
    }
}
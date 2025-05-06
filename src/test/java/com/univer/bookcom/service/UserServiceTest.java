package com.univer.bookcom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserRepository userRepository;
    private CommentsRepository commentsRepository;
    private CacheContainer cacheContainer;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        commentsRepository = mock(CommentsRepository.class);
        cacheContainer = mock(CacheContainer.class);
        userService = new UserService(userRepository, commentsRepository, null, cacheContainer);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetUserById_fromCache() {
        User user = new User();
        user.setId(1L);
        when(cacheContainer.getUserCache()).thenReturn(Map.of(1L, new CacheEntry<>(user)));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(cacheContainer).getUserCache();
    }

    @Test
    void testGetUserById_fromRepository() {
        User user = new User();
        user.setId(1L);
        when(cacheContainer.getUserCache()).thenReturn(new HashMap<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cacheContainer.getUserCache()).thenReturn(new HashMap<>());

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findById(1L);
    }

    @Test
    void testUpdateUser_success() {
        User user = new User();
        user.setId(1L);
        user.setName("Old Name");

        User updatedUser = new User();
        updatedUser.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("New Name", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_notFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        User userToUpdate = new User();

        assertThrows(UserNotFoundException.class, () ->
                userService.updateUser(1L, userToUpdate));
    }

    @Test
    void testDeleteUser_success() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(commentsRepository).deleteAll(anyList());
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
        verify(commentsRepository).deleteAll(user.getComments());
        verify(cacheContainer).getUserCache();
    }

    @Test
    void testDeleteUser_notFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void testAddBookToUser() {
        User user = new User();
        user.setId(1L);
        Book book = new Book();
        book.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.addBookToUser(1L, book);

        assertTrue(user.getBooks().contains(book));
        verify(userRepository).save(user);
    }

    @Test
    void testRemoveBookFromUser() {
        User user = new User();
        user.setId(1L);
        Book book = new Book();
        book.setId(1L);
        user.addBook(book);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.removeBookFromUser(1L, book);

        assertFalse(user.getBooks().contains(book));
        verify(userRepository).save(user);
    }
}
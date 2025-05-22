package com.univer.bookcom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.InvalidBookDataException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserRepository userRepository;
    private CommentsRepository commentsRepository;
    private BookRepository bookRepository;
    private CacheContainer cacheContainer;
    private UserService userService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        commentsRepository = mock(CommentsRepository.class);
        bookRepository = mock(BookRepository.class);
        cacheContainer = mock(CacheContainer.class);
        userService = new UserService(userRepository, commentsRepository,
                bookRepository, cacheContainer);
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

    @Test
    void testSaveUser_shouldSaveAndCache() {
        User user = new User();
        user.setId(1L);

        when(userRepository.save(user)).thenReturn(user);
        Map<Long, CacheEntry<User>> cache = new HashMap<>();
        when(cacheContainer.getUserCache()).thenReturn(cache);

        User result = userService.saveUser(user);

        assertEquals(user, result);
        assertTrue(cache.containsKey(1L));
        verify(userRepository).save(user);
    }

    @Test
    void testFindUsersByName_shouldReturnList() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findByNameContaining("John")).thenReturn(users);

        List<User> result = userService.findUsersByName("John");

        assertEquals(2, result.size());
        verify(userRepository).findByNameContaining("John");
    }

    @Test
    void testFindUserByEmail_shouldReturnOptional() {
        User user = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testAddBooksToUserBulk_addsOnlyNewBooks() {
        User user = new User();
        user.setId(1L);
        user.setBooks(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findByTitleAndCountChaptersAndPublicYearAndStatus(anyString(),
                anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(user)).thenReturn(user);

        Map<Long, CacheEntry<User>> cache = new HashMap<>();
        when(cacheContainer.getUserCache()).thenReturn(cache);

        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setCountChapters(10);
        newBook.setPublicYear(2020);
        newBook.setBookStatus(BookStatus.ANNOUNCED); // Подставь свой статус из enum

        List<Book> booksToAdd = List.of(newBook);

        List<Book> added = userService.addBooksToUserBulk(1L, booksToAdd);

        assertEquals(1, added.size());
        assertTrue(user.getBooks().contains(newBook));
        assertTrue(cache.containsKey(1L));
        verify(bookRepository).save(newBook);
        verify(userRepository).save(user);
    }

    @Test
    void testAddBooksToUserBulk_skipsDuplicatesInUserBooks() {
        User user = new User();
        user.setId(1L);
        user.setBooks(new ArrayList<>());

        Book existingBook = new Book();
        existingBook.setTitle("Duplicate");
        existingBook.setCountChapters(5);
        existingBook.setPublicYear(2010);
        existingBook.setBookStatus(BookStatus.ANNOUNCED);
        user.addBook(existingBook);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Map<Long, CacheEntry<User>> cache = new HashMap<>();
        when(cacheContainer.getUserCache()).thenReturn(cache);

        Book duplicateBook = new Book();
        duplicateBook.setTitle("Duplicate");
        duplicateBook.setCountChapters(5);
        duplicateBook.setPublicYear(2010);
        duplicateBook.setBookStatus(BookStatus.ANNOUNCED);

        List<Book> added = userService.addBooksToUserBulk(1L, List.of(duplicateBook));

        assertTrue(added.isEmpty());
        verify(userRepository).save(user);
        assertTrue(cache.containsKey(1L));
    }

    @Test
    void testAddBooksToUserBulk_skipsDuplicatesAtOtherAuthors() {
        User user = new User();
        user.setId(1L);
        user.setBooks(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Book bookInDb = new Book();
        bookInDb.setTitle("BookInDb");
        bookInDb.setCountChapters(5);
        bookInDb.setPublicYear(2010);
        bookInDb.setBookStatus(BookStatus.ANNOUNCED);
        bookInDb.addAuthor(new User()); // Другой автор

        when(bookRepository.findByTitleAndCountChaptersAndPublicYearAndStatus(
                eq("BookInDb"), eq(5L), eq(2010L), eq(BookStatus.ANNOUNCED)))
                .thenReturn(Optional.of(bookInDb));

        Map<Long, CacheEntry<User>> cache = new HashMap<>();
        when(cacheContainer.getUserCache()).thenReturn(cache);

        Book bookToAdd = new Book();
        bookToAdd.setTitle("BookInDb");
        bookToAdd.setCountChapters(5);
        bookToAdd.setPublicYear(2010);
        bookToAdd.setBookStatus(BookStatus.ANNOUNCED);

        List<Book> added = userService.addBooksToUserBulk(1L, List.of(bookToAdd));

        assertTrue(added.isEmpty());
        verify(userRepository).save(user);
        assertTrue(cache.containsKey(1L));
    }

    @Test
    void testAddBookToUser_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Book book = new Book();
        assertThrows(UserNotFoundException.class, () -> userService.addBookToUser(1L, book));
    }

    @Test
    void testRemoveBookFromUser_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Book book = new Book();
        assertThrows(UserNotFoundException.class, () -> userService.removeBookFromUser(1L, book));
    }

    @Test
    void testAddBooksToUserBulk_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.addBooksToUserBulk(1L, List.of(new Book())));
    }

    @Test
    void deleteUser_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(USER_ID));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void deleteUser_WithCommentsAndBooks_ShouldCleanAllDependencies() {
        User user = new User();
        user.setId(USER_ID);

        List<Comments> comments = Arrays.asList(new Comments(), new Comments());
        user.setComments(comments);

        Book bookWithSingleAuthor = new Book();
        bookWithSingleAuthor.setAuthors(new ArrayList<>(List.of(user)));

        User otherUser = new User();
        otherUser.setId(2L);
        Book bookWithMultipleAuthors = new Book();
        bookWithMultipleAuthors.setAuthors(new ArrayList<>(List.of(user, otherUser)));

        user.setBooks(List.of(bookWithSingleAuthor, bookWithMultipleAuthors));

        Map<Long, CacheEntry<User>> userCacheMock = mock(Map.class);
        when(cacheContainer.getUserCache()).thenReturn(userCacheMock);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.deleteUser(USER_ID);

        verify(commentsRepository).deleteAll(comments);

        verify(bookRepository).delete(bookWithSingleAuthor);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());

        Book savedBook = bookCaptor.getValue();
        assertEquals(1, savedBook.getAuthors().size());
        assertTrue(savedBook.getAuthors().contains(otherUser));

        verify(userRepository).delete(user);
        verify(userCacheMock).remove(USER_ID);
    }

    @Test
    void addBooksToUserBulk_EmptyTitle_ThrowsInvalidBookDataException() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Book invalidBook = new Book();
        invalidBook.setTitle("");
        invalidBook.setCountChapters(5);
        invalidBook.setPublicYear(2010);
        invalidBook.setBookStatus(BookStatus.ANNOUNCED);

        InvalidBookDataException ex = assertThrows(
                InvalidBookDataException.class,
                () -> userService.addBooksToUserBulk(USER_ID, List.of(invalidBook))
        );

        assertEquals("Название книги не может быть пустым", ex.getMessage());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void addBooksToUserBulk_NullTitle_ThrowsInvalidBookDataException() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Book invalidBook = new Book();
        invalidBook.setTitle(null);
        invalidBook.setCountChapters(5);
        invalidBook.setPublicYear(2010);
        invalidBook.setBookStatus(BookStatus.ANNOUNCED);

        InvalidBookDataException ex = assertThrows(
                InvalidBookDataException.class,
                () -> userService.addBooksToUserBulk(USER_ID, List.of(invalidBook))
        );

        assertEquals("Название книги не может быть пустым", ex.getMessage());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void addBooksToUserBulk_NegativeChapters_ThrowsInvalidBookDataException() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Book invalidBook = new Book();
        invalidBook.setTitle("Valid Title");
        invalidBook.setCountChapters(-1L);
        invalidBook.setPublicYear(2010);
        invalidBook.setBookStatus(BookStatus.ANNOUNCED);

        InvalidBookDataException ex = assertThrows(
                InvalidBookDataException.class,
                () -> userService.addBooksToUserBulk(USER_ID, List.of(invalidBook))
        );

        assertEquals("Количество глав не может быть отрицательным", ex.getMessage());
        verify(bookRepository, never()).save(any());
    }


    @Test
    void addBooksToUserBulk_NegativeYear_ThrowsInvalidBookDataException() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Book invalidBook = new Book();
        invalidBook.setTitle("A");
        invalidBook.setCountChapters(5);
        invalidBook.setPublicYear(-2020L);
        invalidBook.setBookStatus(BookStatus.ANNOUNCED);

        InvalidBookDataException ex = assertThrows(
                InvalidBookDataException.class,
                () -> userService.addBooksToUserBulk(USER_ID, List.of(invalidBook))
        );

        assertEquals("Год публикации не может быть отрицательным", ex.getMessage());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void addBooksToUserBulk_NullStatus_ThrowsInvalidBookDataException() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Book invalidBook = new Book();
        invalidBook.setTitle("A");
        invalidBook.setCountChapters(5);
        invalidBook.setPublicYear(2010);
        invalidBook.setBookStatus(null);

        InvalidBookDataException ex = assertThrows(
                InvalidBookDataException.class,
                () -> userService.addBooksToUserBulk(USER_ID, List.of(invalidBook))
        );

        assertEquals("Статус книги не может быть null", ex.getMessage());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void addBooksToUserBulk_EmptyBookList_ReturnsEmptyList() {
        List<Book> result = userService.addBooksToUserBulk(USER_ID, Collections.emptyList());

        assertTrue(result.isEmpty(), "Expected empty list when books is empty");
        verify(bookRepository, never()).save(any());
    }
}
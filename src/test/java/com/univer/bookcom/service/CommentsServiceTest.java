package com.univer.bookcom.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.CommentNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentsServiceTest {

    @Mock
    private CommentsRepository commentsRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheContainer cacheContainer;

    @InjectMocks
    private CommentsService commentsService;

    private Book testBook;
    private User testUser;
    private Comments testComment;
    private Map<Long, CacheEntry<Comments>> commentsCache;

    @BeforeEach
    void setUp() {
        testBook = createTestBook();
        testUser = createTestUser();
        testComment = createTestComment();
        commentsCache = new ConcurrentHashMap<>();

        lenient().when(cacheContainer.getCommentsCache()).thenReturn(commentsCache);
    }

    private Book createTestBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        return book;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("testuser");
        return user;
    }

    private Comments createTestComment() {
        Comments comment = new Comments();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setBook(testBook);
        comment.setUser(testUser);
        return comment;
    }

    @Test
    void createCommentShouldCreateAndReturnComment() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentsRepository.save(any(Comments.class))).thenReturn(testComment);

        Comments result = commentsService.createComment(1L, 1L, "Test comment");

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Test comment", result.getText()),
                () -> assertEquals(testBook, result.getBook()),
                () -> assertEquals(testUser, result.getUser()),
                () -> assertTrue(commentsCache.containsKey(1L)),
                () -> assertEquals(testComment, commentsCache.get(1L).getValue())
        );

        verify(bookRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(commentsRepository).save(any(Comments.class));
    }

    @Test
    void createCommentShouldThrowBookNotFoundException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(BookNotFoundException.class,
                () -> commentsService.createComment(1L, 1L, "Test comment"));

        assertNotNull(exception);
        verify(bookRepository).findById(1L);
        verifyNoInteractions(userRepository, commentsRepository);
        assertFalse(commentsCache.containsKey(1L));
    }

    @Test
    void getCommentsByBookIdShouldReturnCommentsList() {
        when(commentsRepository.findByBookId(1L)).thenReturn(List.of(testComment));

        List<Comments> result = commentsService.getCommentsByBookId(1L);

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(testComment, result.get(0))
        );

        verify(commentsRepository).findByBookId(1L);
        verifyNoInteractions(cacheContainer);
    }

    @Test
    void updateCommentShouldUpdateAndReturnComment() {
        Comments updatedComment = createUpdatedComment();

        when(commentsRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentsRepository.save(any(Comments.class))).thenReturn(updatedComment);

        Comments result = commentsService.updateComment(1L, "Updated comment");

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Updated comment", result.getText()),
                () -> assertTrue(commentsCache.containsKey(1L)),
                () -> assertEquals(updatedComment, commentsCache.get(1L).getValue())
        );

        verify(commentsRepository).findById(1L);
        verify(commentsRepository).save(any(Comments.class));
    }

    private Comments createUpdatedComment() {
        Comments comment = new Comments();
        comment.setId(1L);
        comment.setText("Updated comment");
        comment.setBook(testBook);
        comment.setUser(testUser);
        return comment;
    }

    @Test
    void deleteCommentShouldDeleteComment() {
        when(commentsRepository.findById(1L)).thenReturn(Optional.of(testComment));
        doNothing().when(commentsRepository).delete(testComment);

        commentsService.deleteComment(1L);

        verify(commentsRepository).findById(1L);
        verify(commentsRepository).delete(testComment);
        assertFalse(commentsCache.containsKey(1L));
    }

    @Test
    void createCommentShouldThrowUserNotFoundException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> commentsService.createComment(1L, 1L, "Test comment"));

        assertNotNull(exception);
        verify(bookRepository).findById(1L);
        verify(userRepository).findById(1L);
        verifyNoInteractions(commentsRepository);
        assertFalse(commentsCache.containsKey(1L));
    }

    @Test
    void getCommentsByUserIdShouldReturnCommentsList() {
        when(commentsRepository.findByUserId(1L)).thenReturn(List.of(testComment));

        List<Comments> result = commentsService.getCommentsByUserId(1L);

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(testComment, result.get(0))
        );

        verify(commentsRepository).findByUserId(1L);
        verifyNoInteractions(cacheContainer);
    }

    @Test
    void updateCommentShouldThrowCommentNotFoundException() {
        when(commentsRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CommentNotFoundException.class,
                () -> commentsService.updateComment(1L, "New text"));

        assertNotNull(exception);
        verify(commentsRepository).findById(1L);
        verifyNoMoreInteractions(commentsRepository);
        assertFalse(commentsCache.containsKey(1L));
    }

    @Test
    void deleteCommentShouldThrowCommentNotFoundException() {
        when(commentsRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CommentNotFoundException.class,
                () -> commentsService.deleteComment(1L));

        assertNotNull(exception);
        verify(commentsRepository).findById(1L);
        verifyNoMoreInteractions(commentsRepository);
        assertFalse(commentsCache.containsKey(1L));
    }
}
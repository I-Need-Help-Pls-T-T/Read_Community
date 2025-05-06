package com.univer.bookcom.service;

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
import static org.mockito.Mockito.when;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
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
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");

        testComment = new Comments();
        testComment.setId(1L);
        testComment.setText("Test comment");
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setBook(testBook);
        testComment.setUser(testUser);

        commentsCache = new ConcurrentHashMap<>();
        lenient().when(cacheContainer.getCommentsCache()).thenReturn(commentsCache);
    }

    @Test
    void createComment_ShouldCreateAndReturnComment() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentsRepository.save(any(Comments.class))).thenReturn(testComment);

        Comments result = commentsService.createComment(1L, 1L, "Test comment");

        assertNotNull(result);
        assertEquals("Test comment", result.getText());
        assertEquals(testBook, result.getBook());
        assertEquals(testUser, result.getUser());

        verify(bookRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(commentsRepository).save(any(Comments.class));
        assertTrue(commentsCache.containsKey(1L));
        assertEquals(testComment, commentsCache.get(1L).getValue());
    }

    @Test
    void createComment_ShouldThrowBookNotFoundException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () ->
                commentsService.createComment(1L, 1L, "Test comment"));

        verify(bookRepository).findById(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(commentsRepository);
        assertFalse(commentsCache.containsKey(1L));
    }

    @Test
    void getCommentsByBookId_ShouldReturnCommentsList() {
        when(commentsRepository.findByBookId(1L)).thenReturn(List.of(testComment));

        List<Comments> result = commentsService.getCommentsByBookId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testComment, result.get(0));

        verify(commentsRepository).findByBookId(1L);
        verifyNoInteractions(cacheContainer);
    }

    @Test
    void updateComment_ShouldUpdateAndReturnComment() {
        Comments updatedComment = new Comments();
        updatedComment.setId(1L);
        updatedComment.setText("Updated comment");
        updatedComment.setBook(testBook);
        updatedComment.setUser(testUser);

        when(commentsRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentsRepository.save(any(Comments.class))).thenReturn(updatedComment);

        Comments result = commentsService.updateComment(1L, "Updated comment");

        assertNotNull(result);
        assertEquals("Updated comment", result.getText());

        verify(commentsRepository).findById(1L);
        verify(commentsRepository).save(any(Comments.class));
        assertTrue(commentsCache.containsKey(1L));
        assertEquals(updatedComment, commentsCache.get(1L).getValue());
    }

    @Test
    void deleteComment_ShouldDeleteComment() {
        when(commentsRepository.findById(1L)).thenReturn(Optional.of(testComment));
        doNothing().when(commentsRepository).delete(testComment);

        commentsService.deleteComment(1L);

        verify(commentsRepository).findById(1L);
        verify(commentsRepository).delete(testComment);
        assertFalse(commentsCache.containsKey(1L));
    }
}

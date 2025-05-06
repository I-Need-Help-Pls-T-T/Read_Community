package com.univer.bookcom.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CommentsService {
    private static final Logger log = LoggerFactory.getLogger(CommentsService.class);

    private final CommentsRepository commentsRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CacheContainer cacheContainer;

    public CommentsService(CommentsRepository commentsRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository,
                           CacheContainer cacheContainer) {
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cacheContainer = cacheContainer;
    }

    public Comments createComment(Long bookId, Long userId, String text) {
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new BookNotFoundException("Книга с id " + bookId + " не найдена"));
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("Пользователь с id " + userId + " не найден"));

        Comments comment = new Comments();
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setBook(book);
        comment.setUser(user);

        Comments saved = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(saved.getId(), new CacheEntry<>(saved));
        log.debug("Создан новый комментарий с ID: {}", saved.getId());
        return saved;
    }

    public List<Comments> getCommentsByBookId(Long bookId) {
        List<Comments> comments = commentsRepository.findByBookId(bookId);
        log.debug("Найдено {} комментариев для книги с ID: {}", comments.size(), bookId);
        return comments;
    }

    public List<Comments> getCommentsByUserId(Long userId) {
        List<Comments> comments = commentsRepository.findByUserId(userId);
        log.debug("Найдено {} комментариев пользователя с ID: {}", comments.size(), userId);
        return comments;
    }

    public Comments updateComment(Long commentId, String newText) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException("Комментарий с id " + commentId + " не найден"));
        comment.setText(newText);
        Comments updated = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(commentId, new CacheEntry<>(updated));
        log.debug("Обновлен комментарий с ID: {}", commentId);
        return updated;
    }

    public void deleteComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException("Комментарий с id " + commentId + " не найден"));
        commentsRepository.delete(comment);
        cacheContainer.getCommentsCache().remove(commentId);
        log.debug("Удален комментарий с ID: {}", commentId);
    }
}
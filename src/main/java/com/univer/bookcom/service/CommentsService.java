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
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CommentsService {
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
        return saved;
    }

    public List<Comments> getCommentsByBookId(Long bookId) {
        return commentsRepository.findByBookId(bookId);
    }

    public List<Comments> getCommentsByUserId(Long userId) {
        return commentsRepository.findByUserId(userId);
    }

    public Comments updateComment(Long commentId, String newText) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException("Комментарий с id " + commentId + " не найден"));
        comment.setText(newText);
        Comments updated = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(commentId, new CacheEntry<>(updated));
        return updated;
    }

    public void deleteComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException("Комментарий с id " + commentId + " не найден"));
        commentsRepository.delete(comment);
        cacheContainer.getCommentsCache().remove(commentId);
        System.out.println("Удаление комментария вручную из кэша: id = " + commentId);
    }

    public Optional<Comments> getCommentById(Long commentId) {
        Map<Long, CacheEntry<Comments>> cache = cacheContainer.getCommentsCache();
        if (cache.containsKey(commentId)) {
            System.out.println("Комментарий найден в кэше: id = " + commentId);
            return Optional.of(cache.get(commentId).getValue());
        }

        Optional<Comments> comment = commentsRepository.findById(commentId);
        comment.ifPresent(c -> cache.put(c.getId(), new CacheEntry<>(c)));
        return comment;
    }
}
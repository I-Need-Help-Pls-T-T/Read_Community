package com.univer.bookcom.service;

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
import org.springframework.stereotype.Service;

@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CommentsService(CommentsRepository commentsRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository) {
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
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

        return commentsRepository.save(comment);
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
        return commentsRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException("Комментарий с id " + commentId + " не найден"));
        commentsRepository.delete(comment);
    }
}
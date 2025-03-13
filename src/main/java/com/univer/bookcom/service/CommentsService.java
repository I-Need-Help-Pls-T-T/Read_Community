package com.univer.bookcom.service;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public CommentsService(CommentsRepository commentsRepository, UserRepository
                           userRepository, BookRepository bookRepository) {
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Object> createComment(Comments comment) {
        Optional<User> userOptional = userRepository.findById(comment.getUser().getId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        Optional<Book> bookOptional = bookRepository.findById(comment.getBook().getId());
        if (bookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Книга не найдена");
        }

        comment.setCreatedAt(LocalDateTime.now());

        Comments createdComment = commentsRepository.save(comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    public List<Comments> getAllComments() {
        return commentsRepository.findAll();
    }

    public Comments getCommentById(Long id) {
        return commentsRepository.findById(id).orElse(null);
    }

    public List<Comments> getCommentsByBookId(Long bookId) {
        return commentsRepository.findByBookId(bookId);
    }

    public List<Comments> getCommentsByUserId(Long userId) {
        return commentsRepository.findByUserId(userId);
    }

    public Comments updateComment(Long id, Comments updatedComment) {
        Comments comment = commentsRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setText(updatedComment.getText());
            return commentsRepository.save(comment);
        }
        return null;
    }

    public void deleteComment(Long id) {
        commentsRepository.deleteById(id);
    }
}
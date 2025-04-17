package com.univer.bookcom.controller;

import com.univer.bookcom.exception.CommentNotFoundException;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.service.CommentsService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
public class CommentsController {
    private final CommentsService commentsService;

    public CommentsController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping
    public ResponseEntity<Comments> createComment(
            @RequestParam Long bookId,
            @RequestParam Long userId,
            @RequestParam String text) {
        Comments comment = commentsService.createComment(bookId, userId, text);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Comments>> getCommentsByBookId(@PathVariable Long bookId) {
        List<Comments> comments = commentsService.getCommentsByBookId(bookId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comments>> getCommentsByUserId(@PathVariable Long userId) {
        List<Comments> comments = commentsService.getCommentsByUserId(userId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comments> updateComment(
            @PathVariable Long commentId,
            @RequestParam String newText) {
        try {
            Comments updatedComment = commentsService.updateComment(commentId, newText);
            return ResponseEntity.ok(updatedComment);
        } catch (CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            //commentsService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
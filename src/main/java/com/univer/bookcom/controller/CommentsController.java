package com.univer.bookcom.controller;

import com.univer.bookcom.model.Comments;
import com.univer.bookcom.service.CommentsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody Comments comment) {
        return commentsService.createComment(comment);
    }

    @GetMapping
    public ResponseEntity<List<Comments>> getAllComments() {
        List<Comments> comments = commentsService.getAllComments();
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comments> getCommentById(@PathVariable Long id) {
        Comments comment = commentsService.getCommentById(id);
        return comment != null ? ResponseEntity.ok(comment) : ResponseEntity.notFound().build();
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

    @PutMapping("/{id}")
    public ResponseEntity<Comments> updateComment(@PathVariable Long id,
                                                  @RequestBody Comments updatedComment) {
        Comments comment = commentsService.updateComment(id, updatedComment);
        return comment != null ? ResponseEntity.ok(comment) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentsService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
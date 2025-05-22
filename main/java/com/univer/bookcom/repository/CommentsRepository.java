package com.univer.bookcom.repository;

import com.univer.bookcom.model.Comments;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByBookId(Long bookId);

    List<Comments> findByUserId(Long userId);
}
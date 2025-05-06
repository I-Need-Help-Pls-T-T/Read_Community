package com.univer.bookcom.repository;

import com.univer.bookcom.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);

    @Query("SELECT u FROM User u JOIN u.books b WHERE b.title = :title")
    List<User> findUsersByBookTitle(@Param("title") String title);

    @Query("SELECT u FROM User u JOIN u.books b WHERE b.title = :title")
    List<User> findAuthorsByBookTitle(@Param("title") String title);
}
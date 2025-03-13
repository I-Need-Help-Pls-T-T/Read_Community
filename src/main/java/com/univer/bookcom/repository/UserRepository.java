package com.univer.bookcom.repository;

import com.univer.bookcom.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);

    List<User> findByEmail(String email);

    List<User> findByCountPublicGreaterThanEqual(long countPublic);

    List<User> findByCountTranslateGreaterThanEqual(long countTranslate);
}
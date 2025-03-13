package com.univer.bookcom.repository;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitle(String title);

    List<Book> findByAuthor(String author);

    List<Book> findByPublicYear(long publicYear);

    List<Book> findByCountChaptersGreaterThanEqual(long countChapters);

    List<Book> findByBookStatus(BookStatus status);
}
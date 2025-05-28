package com.univer.bookcom.repository;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitle(String title);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.name = :author")
    List<Book> findByAuthor(@Param("author") String author);

    List<Book> findByPublicYear(long publicYear);

    List<Book> findByStatus(BookStatus status);

    List<Book> findByAuthorsContaining(User author);

    Optional<Book> findByTitleAndCountChaptersAndPublicYearAndStatus(
            String title, long countChapters, long publicYear, BookStatus status
    );

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END "
            + "FROM Book b JOIN b.authors a "
            + "WHERE b.id = :bookId AND a.id = :authorId")
    boolean existsByIdAndAuthorId(@Param("bookId") Long bookId, @Param("authorId") Long authorId);

    @Query("SELECT COUNT(a) "
            + "FROM Book b JOIN b.authors a "
            + "WHERE b.id = :bookId AND a.id != :authorId")
    long countAuthorsByBookIdExcludingAuthor(@Param("bookId") Long bookId,
                                             @Param("authorId") Long authorId);

}
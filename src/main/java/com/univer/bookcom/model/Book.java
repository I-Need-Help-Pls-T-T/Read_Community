package com.univer.bookcom.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private long countChapters;
    private long publicYear;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @SuppressWarnings("checkstyle:Indentation")
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_user",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCountChapters() {
        return countChapters;
    }

    public void setCountChapters(long countChapters) {
        this.countChapters = countChapters;
    }

    public long getPublicYear() {
        return publicYear;
    }

    public void setPublicYear(long publicYear) {
        this.publicYear = publicYear;
    }

    public BookStatus getBookStatus() {
        return status;
    }

    public void setBookStatus(BookStatus bookStatus) {
        this.status = bookStatus;
    }

    public List<User> getAuthors() {
        return authors;
    }

    public void addAuthor(User author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    public void removeAuthor(User author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }
}
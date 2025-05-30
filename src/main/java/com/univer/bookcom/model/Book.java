package com.univer.bookcom.model;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название книги не может быть пустым")
    private String title;

    @Min(value = 0, message = "Количество глав должно быть не менее 1")
    private long countChapters;

    @Min(value = 1000, message = "Год публикации должен быть не ранее 1000")
    @Max(value = 2100, message = "Год публикации должен быть не позднее 2100")
    private long publicYear;

    @NotBlank(message = "Описание книги не может быть пустым")
    @Size(max = 1000, message = "Описание книги не должно превышать 1000 символов")
    private String description;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_user",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    public Book() {
        this.authors = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        if (!this.authors.contains(author)) {
            this.authors.add(author);
            if (!author.getBooks().contains(this)) {
                author.getBooks().add(this);
            }
        }
    }

    public void removeAuthor(User author) {
        this.authors.remove(author);
        author.getBooks().remove(this);
    }

    public void setAuthors(List<User> authors) {
        if (this.authors != null) {
            for (User author : this.authors) {
                author.getBooks().remove(this);
            }
        }
        this.authors = authors != null ? new ArrayList<>(authors) : new ArrayList<>();
        if (authors != null) {
            for (User author : authors) {
                if (!author.getBooks().contains(this)) {
                    author.getBooks().add(this);
                }
            }
        }
    }

    public List<Comments> getComments() {
        return comments;
    }

    public void setComments(List<Comments> comments) {
        this.comments = comments != null ? new ArrayList<>(comments) : new ArrayList<>();
    }
}
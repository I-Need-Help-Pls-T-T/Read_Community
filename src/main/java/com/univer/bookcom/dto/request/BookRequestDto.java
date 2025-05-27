package com.univer.bookcom.dto.request;

import com.univer.bookcom.model.BookStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class BookRequestDto {
    @NotBlank(message = "Название книги не может быть пустым")
    private String title;

    @Min(value = 0, message = "Количество глав должно быть не менее 1")
    private long countChapters;

    @Min(value = 1000, message = "Год публикации должен быть не ранее 1000")
    @Max(value = 2100, message = "Год публикации должен быть не позднее 2100")
    private long publicYear;

    private BookStatus status;

    private List<Long> authorIds;

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

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public List<Long> getAuthorIds() {
        return authorIds;
    }

    public void setAuthorIds(List<Long> authorIds) {
        this.authorIds = authorIds;
    }
}

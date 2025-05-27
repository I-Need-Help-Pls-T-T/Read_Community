package com.univer.bookcom.dto.response;

import com.univer.bookcom.model.BookStatus;
import java.util.List;

public class BookResponseDto {
    private Long id;

    private String title;

    private long countChapters;

    private long publicYear;

    private BookStatus status;

    private List<Long> authorIds;

    private List<Long> commentIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Long> getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(List<Long> commentIds) {
        this.commentIds = commentIds;
    }
}

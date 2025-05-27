package com.univer.bookcom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentsRequestDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 1000, message = "Комментарий не может превышать 1000 символов")
    private String text;

    private Long userId;

    private Long bookId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}

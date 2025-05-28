package com.univer.bookcom.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for creating/updating a comment")
public class CommentsRequestDto {

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 1000, message = "Комментарий не может превышать 1000 символов")
    @JsonProperty(required = true)
    @Schema(description = "Text of the comment", example = "This book is amazing!",
            minLength = 1, maxLength = 1000)
    private String text;

    @NotNull(message = "ID пользователя должен быть указан")
    @JsonProperty(required = true)
    @Schema(description = "ID of the user who made the comment", example = "1")
    private Long userId;

    @NotNull(message = "ID книги должен быть указан")
    @JsonProperty(required = true)
    @Schema(description = "ID of the book the comment is for", example = "1")
    private Long bookId;
}
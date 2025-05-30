package com.univer.bookcom.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for retrieving comment information")
public class CommentsResponseDto {

    @JsonProperty(required = true)
    @Schema(description = "Comment ID", example = "1")
    private Long id;

    @Schema(description = "Text of the comment", example = "This book is amazing!")
    private String text;

    @Schema(description = "Timestamp when the comment was created", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Name of the user who made the comment", example = "John Doe")
    private String userName;

    @Schema(description = "Title of the book the comment is for", example = "The Great Novel")
    private String bookTitle;

    @JsonProperty(required = true)
    @Schema(description = "ID of the user who made the comment", example = "1")
    private Long userId;
}
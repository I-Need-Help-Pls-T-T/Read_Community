package com.univer.bookcom.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for retrieving book information")
public class BookResponseDto {

    @JsonProperty(required = true)
    @Schema(description = "Book ID", example = "1")
    private Long id;

    @Schema(description = "Title of the book", example = "The Great Novel")
    private String title;

    @Schema(description = "Number of chapters in the book", example = "12")
    private Long countChapters;

    @Schema(description = "Publication year of the book", example = "2020")
    private Long publicYear;

    @Schema(description = "Status of the book", example = "PUBLISHED")
    private String bookStatus;

    @Schema(description = "List of author names for the book",
            example = "[\"John Doe\", \"Jane Smith\"]")
    private List<String> authorNames = new ArrayList<>();

    @Schema(description = "List of comment texts for the book",
            example = "[\"Great read!\", \"Very insightful.\"]")
    private List<String> commentTexts = new ArrayList<>();
}
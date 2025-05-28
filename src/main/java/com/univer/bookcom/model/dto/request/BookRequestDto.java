package com.univer.bookcom.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for creating/updating a book")
public class BookRequestDto {

    @NotBlank(message = "Название книги не может быть пустым")
    @JsonProperty(required = true)
    @Schema(description = "Title of the book", example = "The Great Novel", minLength = 1)
    private String title;

    @NotNull(message = "Количество глав должно быть указано")
    @Min(value = 0, message = "Количество глав должно быть не менее 1")
    @JsonProperty(required = true)
    @Schema(description = "Number of chapters in the book", example = "12")
    private Long countChapters;

    @NotNull(message = "Год публикации должен быть указан")
    @Min(value = 1000, message = "Год публикации должен быть не ранее 1000")
    @Max(value = 2100, message = "Год публикации должен быть не позднее 2100")
    @JsonProperty(required = true)
    @Schema(description = "Publication year of the book", example = "2020")
    private Long publicYear;

    @NotNull(message = "Статус книги должен быть указан")
    @JsonProperty(required = true)
    @Schema(description = "Status of the book", example = "PUBLISHED")
    private String bookStatus;

    @Schema(description = "List of author IDs for the book", example = "[1, 2]")
    private List<Long> authorIds = new ArrayList<>();
}
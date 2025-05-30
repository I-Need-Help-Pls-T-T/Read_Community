package com.univer.bookcom.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для создания/обновления книги")
public class BookRequestDto {

    @NotBlank(message = "Название книги не может быть пустым")
    @JsonProperty(required = true)
    @Schema(description = "Название книги", example = "Великий роман", minLength = 1)
    private String title;

    @NotNull(message = "Количество глав должно быть указано")
    @Min(value = 0, message = "Количество глав должно быть не менее 1")
    @JsonProperty(required = true)
    @Schema(description = "Количество глав в книге", example = "12")
    private Long countChapters;

    @NotNull(message = "Год публикации должен быть указан")
    @Min(value = 1000, message = "Год публикации должен быть не ранее 1000")
    @Max(value = 2100, message = "Год публикации должен быть не позднее 2100")
    @JsonProperty(required = true)
    @Schema(description = "Год публикации книги", example = "2020")
    private Long publicYear;

    @NotBlank(message = "Описание книги не может быть пустым")
    @Size(max = 1000, message = "Описание книги не должно превышать 1000 символов")
    @JsonProperty(required = true)
    @Schema(description = "Описание книги", example = "Захватывающий роман о приключениях")
    private String description;

    @NotNull(message = "Статус книги должен быть указан")
    @JsonProperty(required = true)
    @Schema(description = "Статус книги", example = "PUBLISHED")
    private String bookStatus;

    @Schema(description = "Список ID авторов книги", example = "[1, 2]")
    private List<Long> authorIds = new ArrayList<>();
}
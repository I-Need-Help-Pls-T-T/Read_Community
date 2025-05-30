package com.univer.bookcom.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для получения информации о книге")
public class BookResponseDto {

    @JsonProperty(required = true)
    @Schema(description = "ID книги", example = "1")
    private Long id;

    @Schema(description = "Название книги", example = "Великий роман")
    private String title;

    @Schema(description = "Количество глав в книге", example = "12")
    private Long countChapters;

    @Schema(description = "Год публикации книги", example = "2020")
    private Long publicYear;

    @Schema(description = "Описание книги", example = "Захватывающий роман о приключениях")
    private String description;

    @Schema(description = "Статус книги", example = "PUBLISHED")
    private String bookStatus;

    @Schema(description = "Список имен авторов книги",
            example = "[\"Иван Иванов\", \"Мария Петрова\"]")
    private List<String> authorNames = new ArrayList<>();
}
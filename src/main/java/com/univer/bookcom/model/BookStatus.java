package com.univer.bookcom.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус книги")
public enum BookStatus {
    @Schema(description = "Анонсирована")
    ANNOUNCED,

    @Schema(description = "Выходит")
    ONGOING,

    @Schema(description = "Закончена")
    COMPLETED,

    @Schema(description = "Заморожена")
    FROZEN
}

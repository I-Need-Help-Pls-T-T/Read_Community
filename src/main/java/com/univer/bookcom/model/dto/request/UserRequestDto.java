package com.univer.bookcom.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for creating/updating a user")
public class UserRequestDto {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 100, message = "Имя пользователя не может превышать 100 символов")
    @JsonProperty(required = true)
    @Schema(description = "Name of the user", example = "John Doe", minLength = 1, maxLength = 100)
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @JsonProperty(required = true)
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    @JsonProperty(required = true)
    @Schema(description = "User's password", example = "securePass123", minLength = 6)
    private String password;
}
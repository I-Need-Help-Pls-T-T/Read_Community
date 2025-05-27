package com.univer.bookcom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDto {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 100, message = "Имя пользователя не может превышать 100 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

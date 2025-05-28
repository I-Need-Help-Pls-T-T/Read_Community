package com.univer.bookcom.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for retrieving user information")
public class UserResponseDto {

    @JsonProperty(required = true)
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Name of the user", example = "John Doe")
    private String name;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "List of book titles authored by the user",
            example = "[\"Book One\", \"Book Two\"]")
    private List<String> bookTitles = new ArrayList<>();

    @Schema(description = "List of comment texts made by the user",
            example = "[\"Great book!\", \"Needs more details.\"]")
    private List<String> commentTexts = new ArrayList<>();
}

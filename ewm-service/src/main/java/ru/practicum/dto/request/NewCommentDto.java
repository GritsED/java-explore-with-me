package ru.practicum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank
    @Size(min = 1, max = 2000)
    String text;
}

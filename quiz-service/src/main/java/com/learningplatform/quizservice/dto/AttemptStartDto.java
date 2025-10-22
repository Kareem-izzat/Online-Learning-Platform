package com.learningplatform.quizservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptStartDto {

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Student ID is required")
    private Long studentId;
}

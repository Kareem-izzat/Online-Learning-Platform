package com.learningplatform.quizservice.dto;

import com.learningplatform.quizservice.entity.QuestionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequestDto {

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must be less than 1000 characters")
    private String questionText;

    private String imageUrl;

    private List<String> options;  // For multiple choice/select

    private List<String> correctAnswers;  // Correct option(s)

    @Size(max = 1000, message = "Explanation must be less than 1000 characters")
    private String explanation;

    @Min(value = 0, message = "Points cannot be negative")
    private Double points;

    private Integer orderIndex;
}

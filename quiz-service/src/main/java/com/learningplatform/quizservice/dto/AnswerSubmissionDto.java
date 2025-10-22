package com.learningplatform.quizservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSubmissionDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    private List<String> selectedOptions;  // For multiple choice/select

    private String textAnswer;  // For short answer/essay
}

package com.learningplatform.quizservice.dto;

import com.learningplatform.quizservice.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDto {

    private Long id;
    private Long quizId;
    private QuestionType type;
    private String questionText;
    private String imageUrl;
    private List<String> options;
    private List<String> correctAnswers;  // Only shown to instructors or after submission
    private String explanation;
    private Double points;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

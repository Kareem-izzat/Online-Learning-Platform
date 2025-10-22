package com.learningplatform.quizservice.entity;

public enum QuestionType {
    MULTIPLE_CHOICE,    // One correct answer from multiple options
    MULTIPLE_SELECT,    // Multiple correct answers
    TRUE_FALSE,         // Boolean question
    SHORT_ANSWER,       // Text input (manual grading)
    ESSAY              // Long text (manual grading)
}

package com.learnit.discussion.dto;

import com.learnit.discussion.entity.ThreadCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @NotNull(message = "Category is required")
    private ThreadCategory category;

    @Size(max = 5, message = "Maximum 5 tags allowed")
    private Set<String> tags;

    private Boolean isPinned = false;
}

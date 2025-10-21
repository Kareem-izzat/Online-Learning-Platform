package com.learningplatform.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequestDto {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotBlank(message = "Reason is required")
    private String reason;
}

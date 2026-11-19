package edu.academic.solutions;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ActivityRequest(
        @NotBlank String moduleKey,
        @NotBlank @Size(max = 80) String studentId,
        @NotBlank @Size(max = 120) String studentName,
        @NotBlank @Size(max = 160) String title,
        @Size(max = 120) String period,
        @Size(max = 2000) String details,
        @DecimalMin("0.0") BigDecimal metric,
        @DecimalMin("0.01") BigDecimal maximumMetric,
        @NotNull @DecimalMin(value = "0.01") BigDecimal weight,
        @NotBlank @Size(max = 30) String status
) {}

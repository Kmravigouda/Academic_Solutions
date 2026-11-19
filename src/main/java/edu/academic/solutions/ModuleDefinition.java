package edu.academic.solutions;

public record ModuleDefinition(
        String key,
        String title,
        String description,
        String metricLabel,
        String maximumMetricLabel,
        String periodLabel,
        String weightLabel
) {}

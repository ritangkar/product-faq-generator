package com.ritangkar.faqgenerator.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

public record ProductFaqInput(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String brand,
        @Positive double price,
        List<String> colors,
        List<String> features,
        Map<String, String> specifications,
        String targetAudience,
        String useCase
) {
    public ProductFaqInput {
        if (colors == null) colors = List.of();
        if (features == null) features = List.of();
        if (specifications == null) specifications = Map.of();
    }
}

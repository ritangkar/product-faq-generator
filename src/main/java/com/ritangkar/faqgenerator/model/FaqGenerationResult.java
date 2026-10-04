package com.ritangkar.faqgenerator.model;

import java.util.List;

public record FaqGenerationResult(
        List<FaqItem> faqs,
        List<FaqIssue> unsupportedAnswerIssues,
        List<FaqIssue> qualityIssues
) {
}

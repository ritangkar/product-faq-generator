package com.ritangkar.faqgenerator.service;

import com.ritangkar.faqgenerator.model.FaqIssue;
import com.ritangkar.faqgenerator.model.FaqItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Structural quality rules — independent of whether an answer is
 * grounded (see {@link UnsupportedAnswerDetectionService} for that).
 * These catch a different class of problem: a well-grounded FAQ can
 * still be badly formed (duplicated, empty, or trivially circular).
 */
@Service
public class FaqQualityService {

    private static final int MIN_ANSWER_LENGTH = 3;

    public List<FaqIssue> check(List<FaqItem> faqs) {
        List<FaqIssue> issues = new ArrayList<>();
        Set<String> seenQuestions = new HashSet<>();

        for (FaqItem faq : faqs) {
            if (!faq.question().trim().endsWith("?")) {
                issues.add(new FaqIssue(faq.question(), "WARNING", "Question does not end with a question mark."));
            }
            if (faq.answer() == null || faq.answer().trim().length() < MIN_ANSWER_LENGTH) {
                issues.add(new FaqIssue(faq.question(), "ERROR", "Answer is empty or too short to be useful."));
            }
            if (faq.answer() != null && faq.answer().trim().equalsIgnoreCase(faq.question().trim())) {
                issues.add(new FaqIssue(faq.question(), "ERROR", "Answer simply repeats the question."));
            }
            if (!seenQuestions.add(faq.question())) {
                issues.add(new FaqIssue(faq.question(), "WARNING", "Duplicate question generated more than once."));
            }
        }
        return issues;
    }
}

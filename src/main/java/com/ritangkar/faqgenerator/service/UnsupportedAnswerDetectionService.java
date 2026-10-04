package com.ritangkar.faqgenerator.service;

import com.ritangkar.faqgenerator.model.FaqIssue;
import com.ritangkar.faqgenerator.model.FaqItem;
import com.ritangkar.faqgenerator.model.ProductFaqInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Independently re-checks every generated answer against the original
 * input — never trusts {@link FaqTemplateGenerator}'s "grounded by
 * construction" guarantee alone. Two targeted checks, each proven by a
 * dedicated test with a deliberately fabricated example:
 * <ol>
 *   <li>Any number+unit claim (e.g. "250g") must appear in the supplied
 *       features or specifications.</li>
 *   <li>Any recognized color word mentioned in an answer must be one of
 *       the product's supplied colors.</li>
 * </ol>
 * This is what makes the guarantee hold even if a future generator
 * (e.g. an LLM-backed one) replaced {@link FaqTemplateGenerator} —
 * the check doesn't know or care which generator produced the answer.
 */
@Service
public class UnsupportedAnswerDetectionService {

    private static final Pattern NUMBER_WITH_UNIT = Pattern.compile(
            "\\b\\d+(\\.\\d+)?\\s?(kg|g|mm|cm|m|l|ml|w|kw|mah|gb|tb|hrs?|hours?|%|inch(es)?|lbs?)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final List<String> KNOWN_COLOR_WORDS = List.of(
            "black", "white", "red", "blue", "navy", "grey", "gray", "green", "orange", "olive", "brown");

    public List<FaqIssue> check(List<FaqItem> faqs, ProductFaqInput source) {
        String groundTruth = buildGroundTruth(source);
        List<String> allowedColors = source.colors().stream().map(c -> c.toLowerCase(Locale.ROOT)).toList();

        List<FaqIssue> issues = new ArrayList<>();
        for (FaqItem faq : faqs) {
            checkNumberClaims(faq, groundTruth, issues);
            checkColorClaims(faq, allowedColors, issues);
        }
        return issues;
    }

    private void checkNumberClaims(FaqItem faq, String groundTruth, List<FaqIssue> issues) {
        Matcher matcher = NUMBER_WITH_UNIT.matcher(faq.answer());
        while (matcher.find()) {
            String claim = matcher.group().trim();
            if (!groundTruth.contains(claim.toLowerCase(Locale.ROOT))) {
                issues.add(new FaqIssue(faq.question(), "ERROR",
                        "Answer states \"" + claim + "\", which does not appear in the supplied product data."));
            }
        }
    }

    private void checkColorClaims(FaqItem faq, List<String> allowedColors, List<FaqIssue> issues) {
        String answerLower = faq.answer().toLowerCase(Locale.ROOT);
        for (String color : KNOWN_COLOR_WORDS) {
            if (!containsWord(answerLower, color)) continue;
            String canonical = color.equals("gray") ? "grey" : color;
            if (!allowedColors.contains(canonical) && !allowedColors.contains(color)) {
                issues.add(new FaqIssue(faq.question(), "ERROR",
                        "Answer mentions color \"" + color + "\", which is not among the supplied colors " + allowedColors + "."));
            }
        }
    }

    private boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + word + "\\b").matcher(text).find();
    }

    private String buildGroundTruth(ProductFaqInput source) {
        StringBuilder sb = new StringBuilder();
        source.features().forEach(f -> sb.append(f.toLowerCase(Locale.ROOT)).append(' '));
        source.specifications().values().forEach(v -> sb.append(v.toLowerCase(Locale.ROOT)).append(' '));
        return sb.toString();
    }
}

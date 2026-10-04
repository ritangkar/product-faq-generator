package com.ritangkar.faqgenerator.service;

import com.ritangkar.faqgenerator.model.FaqItem;
import com.ritangkar.faqgenerator.model.ProductFaqInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Every FAQ this generator produces is built from exactly one supplied
 * field (or a small, fixed combination) — there is no code path here that
 * can state a fact the input didn't provide. A field that wasn't
 * supplied (e.g. no {@code useCase}) simply means that FAQ is skipped,
 * never filled with a guess.
 */
@Service
public class FaqTemplateGenerator {

    public List<FaqItem> generate(ProductFaqInput input) {
        List<FaqItem> faqs = new ArrayList<>();
        String productLabel = input.brand() + " " + input.name();

        faqs.add(new FaqItem("What category does the " + input.name() + " belong to?",
                "The " + productLabel + " is a " + input.category() + " product.", "General"));

        faqs.add(new FaqItem("How much does the " + input.name() + " cost?",
                "The " + productLabel + " is priced at \u20B9" + formatPrice(input.price()) + ".", "Pricing"));

        if (!input.colors().isEmpty()) {
            faqs.add(new FaqItem("What color options are available for the " + input.name() + "?",
                    "The " + productLabel + " is available in: " + String.join(", ", input.colors()) + ".",
                    "Availability"));
        }

        if (!input.features().isEmpty()) {
            faqs.add(new FaqItem("What are the key features of the " + input.name() + "?",
                    String.join(", ", input.features()) + ".", "Features"));
        }

        input.specifications().forEach((key, value) ->
                faqs.add(new FaqItem("What is the " + key.toLowerCase() + " of the " + input.name() + "?",
                        value + ".", "Specifications")));

        if (input.useCase() != null && !input.useCase().isBlank()) {
            faqs.add(new FaqItem("What is the " + input.name() + " best suited for?",
                    "The " + productLabel + " is designed for " + input.useCase() + ".", "Usage"));
        }

        if (input.targetAudience() != null && !input.targetAudience().isBlank()) {
            faqs.add(new FaqItem("Who is the " + input.name() + " designed for?",
                    "The " + productLabel + " is designed for " + input.targetAudience() + ".", "Usage"));
        }

        return faqs;
    }

    private String formatPrice(double price) {
        return String.format("%,.0f", price);
    }
}

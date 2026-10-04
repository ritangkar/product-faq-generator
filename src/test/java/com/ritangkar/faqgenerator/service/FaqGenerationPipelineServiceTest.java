package com.ritangkar.faqgenerator.service;

import com.ritangkar.faqgenerator.model.FaqGenerationResult;
import com.ritangkar.faqgenerator.model.FaqItem;
import com.ritangkar.faqgenerator.model.ProductFaqInput;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FaqGenerationPipelineServiceTest {

    private final FaqTemplateGenerator generator = new FaqTemplateGenerator();
    private final UnsupportedAnswerDetectionService unsupportedAnswerDetectionService = new UnsupportedAnswerDetectionService();
    private final FaqQualityService faqQualityService = new FaqQualityService();
    private final FaqGenerationPipelineService pipeline = new FaqGenerationPipelineService(
            generator, unsupportedAnswerDetectionService, faqQualityService);

    private ProductFaqInput referenceInput() {
        return new ProductFaqInput("Drift 271", "Footwear", "ZenithWear", 7499.0,
                List.of("Black", "Grey"), List.of("Breathable mesh upper", "Cushioned midsole"),
                Map.of("Weight", "250g"), "long-distance runners", "long-distance running");
    }

    @Test
    void generatesSevenFaqsForAFullyPopulatedInput() {
        FaqGenerationResult result = pipeline.run(referenceInput());
        assertThat(result.faqs()).hasSize(7);
    }

    @Test
    void everyExpectedFaqCategoryIsPresent() {
        FaqGenerationResult result = pipeline.run(referenceInput());
        List<String> categories = result.faqs().stream().map(FaqItem::category).distinct().toList();
        assertThat(categories).containsExactlyInAnyOrder("General", "Pricing", "Availability", "Features", "Specifications", "Usage");
    }

    @Test
    void thePricingAnswerFormatsTheSuppliedPriceExactly() {
        FaqGenerationResult result = pipeline.run(referenceInput());
        var pricingFaq = result.faqs().stream().filter(f -> f.category().equals("Pricing")).findFirst().orElseThrow();
        assertThat(pricingFaq.answer()).isEqualTo("The ZenithWear Drift 271 is priced at \u20B97,499.");
    }

    @Test
    void aFullyGroundedInputProducesNoUnsupportedAnswerIssues() {
        FaqGenerationResult result = pipeline.run(referenceInput());
        assertThat(result.unsupportedAnswerIssues()).isEmpty();
    }

    @Test
    void aFullyGroundedInputProducesNoQualityIssues() {
        FaqGenerationResult result = pipeline.run(referenceInput());
        assertThat(result.qualityIssues()).isEmpty();
    }

    @Test
    void omittingOptionalFieldsSkipsTheCorrespondingFaqsRatherThanGuessing() {
        ProductFaqInput minimal = new ProductFaqInput("Basic Tee", "Apparel", "CoreFit", 899.0,
                List.of(), List.of(), Map.of(), null, null);

        FaqGenerationResult result = pipeline.run(minimal);

        // Only General and Pricing are always generated; everything else needs its field.
        assertThat(result.faqs()).hasSize(2);
        assertThat(result.faqs()).noneMatch(f -> f.category().equals("Usage"));
        assertThat(result.faqs()).noneMatch(f -> f.category().equals("Availability"));
    }

    // --- Proof that the unsupported-answer detector actually catches fabrications ---

    @Test
    void detectsAFabricatedNumericClaimNotPresentInTheSourceData() {
        ProductFaqInput source = referenceInput();
        FaqItem fabricated = new FaqItem("How much does it weigh?", "It weighs only 90g.", "Specifications");

        List<com.ritangkar.faqgenerator.model.FaqIssue> issues =
                unsupportedAnswerDetectionService.check(List.of(fabricated), source);

        assertThat(issues).anyMatch(i -> i.message().contains("90g"));
    }

    @Test
    void detectsAColorClaimNotAmongTheSuppliedColors() {
        ProductFaqInput source = referenceInput(); // colors: Black, Grey only
        FaqItem fabricated = new FaqItem("What colors are available?", "It also comes in Orange.", "Availability");

        List<com.ritangkar.faqgenerator.model.FaqIssue> issues =
                unsupportedAnswerDetectionService.check(List.of(fabricated), source);

        assertThat(issues).anyMatch(i -> i.message().contains("orange"));
    }

    @Test
    void aMentionedColorThatIsInTheAllowedListIsNotFlagged() {
        ProductFaqInput source = referenceInput();
        FaqItem legit = new FaqItem("What colors?", "Available in Black.", "Availability");

        List<com.ritangkar.faqgenerator.model.FaqIssue> issues =
                unsupportedAnswerDetectionService.check(List.of(legit), source);

        assertThat(issues).isEmpty();
    }

    // --- Quality check proofs ---

    @Test
    void detectsAnAnswerThatJustRepeatsTheQuestion() {
        FaqItem circular = new FaqItem("What is this?", "What is this?", "General");
        List<com.ritangkar.faqgenerator.model.FaqIssue> issues = faqQualityService.check(List.of(circular));
        assertThat(issues).anyMatch(i -> i.message().contains("repeats the question"));
    }

    @Test
    void detectsADuplicateQuestion() {
        FaqItem a = new FaqItem("What is the price?", "\u20B9100.", "Pricing");
        FaqItem b = new FaqItem("What is the price?", "\u20B9200.", "Pricing");
        List<com.ritangkar.faqgenerator.model.FaqIssue> issues = faqQualityService.check(List.of(a, b));
        assertThat(issues).anyMatch(i -> i.message().contains("Duplicate question"));
    }
}

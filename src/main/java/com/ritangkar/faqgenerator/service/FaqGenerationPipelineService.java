package com.ritangkar.faqgenerator.service;

import com.ritangkar.faqgenerator.model.FaqGenerationResult;
import com.ritangkar.faqgenerator.model.ProductFaqInput;
import org.springframework.stereotype.Service;

@Service
public class FaqGenerationPipelineService {

    private final FaqTemplateGenerator generator;
    private final UnsupportedAnswerDetectionService unsupportedAnswerDetectionService;
    private final FaqQualityService faqQualityService;

    public FaqGenerationPipelineService(FaqTemplateGenerator generator,
                                         UnsupportedAnswerDetectionService unsupportedAnswerDetectionService,
                                         FaqQualityService faqQualityService) {
        this.generator = generator;
        this.unsupportedAnswerDetectionService = unsupportedAnswerDetectionService;
        this.faqQualityService = faqQualityService;
    }

    public FaqGenerationResult run(ProductFaqInput input) {
        var faqs = generator.generate(input);
        var unsupported = unsupportedAnswerDetectionService.check(faqs, input);
        var quality = faqQualityService.check(faqs);
        return new FaqGenerationResult(faqs, unsupported, quality);
    }
}

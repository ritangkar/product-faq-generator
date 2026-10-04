package com.ritangkar.faqgenerator.web;

import com.ritangkar.faqgenerator.model.FaqGenerationResult;
import com.ritangkar.faqgenerator.model.ProductFaqInput;
import com.ritangkar.faqgenerator.service.FaqGenerationPipelineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaqController {

    private final FaqGenerationPipelineService pipelineService;

    public FaqController(FaqGenerationPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/api/faq/generate")
    public FaqGenerationResult generate(@Valid @RequestBody ProductFaqInput input) {
        return pipelineService.run(input);
    }
}

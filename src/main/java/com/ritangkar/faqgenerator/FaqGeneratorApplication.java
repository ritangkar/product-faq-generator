package com.ritangkar.faqgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Product FAQ Generator.
 *
 * Structured Input -> Grounded Generation -> Unsupported-Answer Detection
 * -> FAQ Quality Checks -> FAQ-ready Structured Output.
 *
 * Every FAQ answer is built directly from the supplied product data by a
 * fixed set of templates — the generator has no mechanism to state a fact
 * it wasn't given. UnsupportedAnswerDetectionService then independently
 * re-verifies that guarantee rather than trusting it by construction
 * alone. See docs/design-decisions.md.
 */
@SpringBootApplication
public class FaqGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaqGeneratorApplication.class, args);
    }
}

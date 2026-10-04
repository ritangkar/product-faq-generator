# Product FAQ Generator

Generates FAQ content strictly grounded in supplied product data:
**Structured Input → Grounded Generation → Unsupported-Answer Detection →
FAQ Quality Checks → FAQ-ready Structured Output.** The system never
invents a product fact — proven, not just claimed.

Part of the [Commerce Engineering Lab](https://ritangkar.github.io/#commerce-lab) —
17 small, production-minded engineering capabilities by Ritangkar Dey.

---

## 1. Project Overview

Send structured product data (name, category, brand, price, colors,
features, specifications, target audience, use case); get back a set of
FAQ question/answer pairs, each traceable to exactly the field that
produced it, plus two independent validation passes.

## 2. Problem

Auto-generated FAQ content is only useful if a customer can trust it. A
generator that occasionally invents a spec or a color option to sound
more complete does real damage — the whole point of an FAQ section is
that the answers are dependable.

## 3. Solution

Every FAQ is built directly from one supplied field — grounded by
construction, not by hope — and then independently re-checked by a
detector that doesn't trust the generator, proven with deliberately
fabricated test cases the detector actually catches.

## 4. Key Features

- FAQs across six categories: General, Pricing, Availability, Features,
  Specifications, Usage
- A missing optional field skips its FAQ rather than guessing a
  placeholder answer
- **Unsupported-answer detection**, proven — not assumed — to catch a
  fabricated numeric claim ("90g" not in the source data) and a
  fabricated color claim ("Orange" not in the supplied colors)
- Structural FAQ quality checks: duplicate questions, answers that just
  echo the question, empty/too-short answers

## 5. Architecture

```
ProductFaqInput → FaqTemplateGenerator (grounded by construction)
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
  UnsupportedAnswerDetection    FaqQualityService
  (independent re-check)        (structural rules)
              └───────────┬───────────┘
                          ▼
                FaqGenerationResult
```

See [`docs/architecture.md`](docs/architecture.md) for the full diagram.

## 6. Technical Approach

`UnsupportedAnswerDetectionService` takes the generated FAQs and the
original input as two separate arguments and re-derives ground truth from
the input every time — it never trusts the generator, which is what makes
it meaningful as a genuine safety net rather than a formality.

## 7. Design Decisions

See [`docs/design-decisions.md`](docs/design-decisions.md) — including why
a broad "every word must appear in the source" check was rejected in favor
of two narrow, reliable ones.

## 8. Sample Input

```json
{
  "name": "Drift 271", "category": "Footwear", "brand": "ZenithWear", "price": 7499.0,
  "colors": ["Black", "Grey"],
  "features": ["Breathable mesh upper", "Cushioned midsole"],
  "specifications": {"Weight": "250g"},
  "targetAudience": "long-distance runners", "useCase": "long-distance running"
}
```

## 9. Sample Output

Real, verified output for the input above (7 FAQs, abridged):

```json
{
  "faqs": [
    { "question": "How much does the Drift 271 cost?", "answer": "The ZenithWear Drift 271 is priced at \u20B97,499.", "category": "Pricing" },
    { "question": "What is the weight of the Drift 271?", "answer": "250g.", "category": "Specifications" },
    { "question": "What is the Drift 271 best suited for?", "answer": "The ZenithWear Drift 271 is designed for long-distance running.", "category": "Usage" }
  ],
  "unsupportedAnswerIssues": [],
  "qualityIssues": []
}
```

## 10. How to Run

```bash
git clone <your-repo-url>
cd product-faq-generator
mvn spring-boot:run
```

API available at `http://localhost:8095`.

## 11. How to Test

```bash
mvn test
```

Covers the full pipeline against a verified reference scenario, confirms
optional-field omission skips FAQs rather than guessing, and — most
importantly — proves the unsupported-answer detector catches a
deliberately fabricated numeric claim and a deliberately fabricated color
claim, constructed independently of the real generator.

## 12. API Documentation

| Method | Path | Description |
|---|---|---|
| POST | `/api/faq/generate` | `ProductFaqInput` returns `FaqGenerationResult` |

## 13. Portfolio Demo

The Commerce Engineering Lab demo (`lab/demos/product-faq-generator.js`)
ports the full pipeline to client-side JavaScript. See [`docs/demo.md`](docs/demo.md).

## 14. Limitations

- Fixed FAQ templates, not free-form generation
- The unsupported-answer detector only catches number+unit and color
  claims — not every possible kind of fabrication (documented plainly)
- English only

## 15. Future Enhancements

- Additional targeted checks (e.g. material-name claims against a known
  materials list)
- An optional live-LLM generator behind the same detector seam
- FAQ re-ordering by predicted customer relevance

## 16. License

MIT — see [`LICENSE`](LICENSE).

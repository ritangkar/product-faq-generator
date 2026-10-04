# Portfolio Demo

**Demo level:** 1 — client-side, fully deterministic.

`lab/demos/product-faq-generator.js` ports the full pipeline — grounded
generation, unsupported-answer detection, and FAQ quality checks — to
client-side JavaScript. Fill in a product's details and see the generated
FAQ set update live, including watching FAQs disappear when you clear an
optional field (proving nothing gets guessed to fill the gap) and watching
the unsupported-answer detector flag a deliberately-fabricated example.
No backend call.

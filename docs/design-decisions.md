# Design Decisions

---

**Decision:** Two narrow, targeted unsupported-answer checks (number+unit
claims, color claims) rather than one broad word-overlap check across the
whole answer.

**Why:** An early design considered checking that every "content word" in
an answer appears somewhere in the source data. That would immediately
false-positive on ordinary template scaffolding — words like "designed,"
"available," "priced," "product" are part of the *template's* English,
not a claim about the product, and would never appear verbatim in a
features list or specifications map. Narrowing to two specific,
checkable claim types (a number with a unit; a named color) avoids that
false-positive problem entirely while still catching the failure mode
that matters: an invented specification or an invented color option.

**Trade-off:** A fabricated claim that isn't a number+unit or a color
(an invented material name, say) wouldn't be caught by either check —
documented plainly, the same honest limitation as the SEO Product
Description Generator project's factuality check, which uses the same
number+unit pattern for the same reason.

---

**Decision:** `UnsupportedAnswerDetectionService` takes the generated
FAQs and the original input as two separate method arguments, and
re-derives its own ground truth from the input every time, rather than
trusting any "this is grounded" signal from the generator.

**Why:** This is what makes the guarantee hold regardless of which
generator produced the FAQs — proven directly in the test suite by
constructing a fabricated `FaqItem` by hand (not through
`FaqTemplateGenerator` at all) and confirming the detector still catches
it. A generator-trusting design would only ever be as safe as the
generator's own promises; this one doesn't rely on that.

---

**Decision:** A missing optional field (`useCase`, `targetAudience`, empty
`colors`) skips its corresponding FAQ entirely, rather than generating a
generic placeholder answer.

**Why:** A "Who is this for?" FAQ answered with something like "a wide
range of customers" when no target audience was actually supplied would
be exactly the kind of unsupported, made-up-sounding claim this whole
project exists to prevent — even though it's vague rather than specific.
Skipping is the honest choice; the generated FAQ set is simply shorter
for a less-complete product listing, and the test suite verifies this directly.

# MyCalculator Independent Audit Report

**Scope:** Forensic, read-only comparison of the native Android port (`/Users/mkshaon/playground/MyCalculator`) against the original Cordova app (`/Users/mkshaon/playground/schulrechner`) and the build spec (`AGENT_BUILD_INSTRUCTIONS.md`) that Google Antigravity/Gemini was given.
**Method:** Four independent deep-read audits (math engine, input state machine, formatting/rendering, feature completeness vs. spec), each reading the relevant source files on both sides in full — not skimming. Several of the most consequential claims below were re-verified directly by grep/read before being included here. No code was modified as part of this audit.

**No files in the app were changed.** This report is new and adds nothing to `git status` except itself.

---

## TL;DR

- **The bug you found (`sin(90)` needing an extra `)`) is not a regression.** It's confirmed byte-for-byte identical behavior to the original — `sin` already opens its own bracket in *both* apps, so if you also press `(` yourself you owe it two `)`. Full explanation below. The correct sequence in both apps is `sin, 9, 0, ), =` — no leading `(`.
- **The math engine core is genuinely solid.** Constants, sexagesimal parsing, operator precedence, the subres (∫/d/dx) machinery, and the overall function set are faithful, verified ports. Two narrow numerical-precision issues were found (gamma function uses a different Lanczos table than mathjs; one CMPLX-mode error-detection rule doesn't match the spec's own formula).
- **The visual rendering layer is where the real corner-cutting is.** This is the part that "101/101 golden tests pass" does **not** cover, because the golden tests only compare HTML-like strings, not actual on-screen pixels. I independently verified: a text-sizing flag (`isSmall`) that's declared but never set anywhere, so every fraction/exponent/subscript is measured smaller than it's drawn; `<br>` line breaks that are silently dropped; the integral (`∫`) construct has no rendering support at all (falls through to a generic flattening branch); the √ radical never stretches to match content height; and display scrolling doesn't follow the cursor (always anchors to the end of the expression).
- **A few explicitly-required features are stubs**, most notably "Turn Off = Close App," which shows a wrong-text alert and never actually closes the app — confirmed by grep, `finishAndRemoveTask()` is mentioned only in a comment, never called.
- **Test-coverage claims overstate what's actually verified.** The 101 golden tests are real and pass, but they only exercise the math-engine + tree-builder string output. The ViewModel, Settings, and the entire visual rendering pipeline (exactly where the bugs above live) have **zero** test coverage, and the three UI tests the spec explicitly names were never written (the `androidTest` file is still the unmodified Android Studio template stub).

---

## 1. Your reported bug: `sin(90)` needing a second `)`

**Verdict: not a port regression — confirmed identical design in both apps.**

Tracing the exact keypress sequence `sin, (, 9, 0, ), =`:

- Pressing **`sin`** already inserts an opening bracket as part of the function: `TreeBuilder.kt`'s `key_sin` case creates a node with `expr = "sind("` — this is one open paren, already spent.
- Pressing **`(`** yourself inserts a *second*, independent open paren (`key_lparen` → `expr = "("`).
- `90` is typed.
- One **`)`** closes only the second (manual) paren.
- The concatenated expression sent to the evaluator is `sind((90)` — two opens, one close, unbalanced — so it fails to parse and shows "error." A second `)` balances it to `sind((90))`, which evaluates fine.

I had the audit agent trace the **original** `logic.js`'s `Sin_Element` the same way: it is built from the exact same `"brackets_operation"` node kind with `mathjs_value` ending in `"sind("` — i.e., the original's `sin` key **also** auto-opens its own bracket. There is no bracket-auto-balancing anywhere in the original's evaluate-on-`=` path either (confirmed by reading the whole file — none exists). Feeding the original's real mathjs engine the string `"sind((90)"` throws the identical "unbalanced parenthesis" error. Every other function key (`cos`, `tan`, inverse trig, hyperbolic, `log`, `ln`) uses the same "auto-opens its own bracket" pattern consistently on both sides — this is not a one-off inconsistency introduced by the port, it's how the calculator has always worked.

**The correct keystroke sequence on both apps is `sin, 9, 0, ), =`** (no leading `(` — the `sin` key already supplies it). If you type the extra `(` out of habit, you need the extra `)` to match, on the original too.

One related, real gap was found in the same area (see **Finding M1** below): pressing Up/Down after deleting an equation back to completely empty does nothing in the port, whereas the original uses that as a shortcut to jump into equation history browsing.

---

## 2. Critical findings

### C1 — The display renderer still works by building HTML-like strings and re-parsing them at runtime, contradicting the spec's explicit instruction
- **Spec:** Phase 7.1 says the render pass should build the display tree directly — *"no need to build an HTML string"* — and Phase 9.1 says the HTML→node parser should live only in the test source set.
- **Actual:** `TreeBuilder.kt` still builds literal strings with the original's CSS class names (`"<span class='frac_wrapper'><span class='frac_top'>"`, `sqrt_wrapper`, `logn_bottom`, `pow_top`, etc.). `HtmlToDisplayNode.kt` was placed in `app/src/main/java/com/my/calculator/display/` — **confirmed by directory listing, it is in the main (production) source set**, not test-only — and is called live on every recomposition from `DisplayLayer.kt`.
- **Why it matters:** this is architecturally the same design as the web app, just re-parsed instead of browser-rendered. It also explains why golden tests "pass 100%" almost trivially: they're comparing near-identical HTML strings to the original, not exercising an independently-built native typesetting engine. All of the rendering bugs below (isSmall, LineBreak, integral, sqrt scaling) live entirely inside this HTML round-trip and are invisible to the golden test suite.

### C2 — "Turn Off = Close App" doesn't close the app
- **Spec:** pressing power-off should call `Activity.finishAndRemoveTask()`, or on failure show a two-button ("Cancel"/"Okay") alert offering to disable the close-on-power-off setting.
- **Actual (confirmed by grep):** `finishAndRemoveTask` appears **exactly once** in the whole codebase — inside a comment in `CalculatorViewModel.kt:115` (`// Handled at Activity level via finishAndRemoveTask or showAlert`). It is never actually called anywhere. The alert shown is a generic single-button "OK" with the wrong copy ("App turned off."), not the spec's two-button "can't close, disable this?" flow, and it doesn't write back to settings either way.
- **Impact:** pressing the power-off key does nothing except show an incorrect message. This is on the spec's own final-acceptance checklist.

### C3 — Fraction/exponent/subscript text is measured smaller than it's drawn (verified directly)
- I confirmed via grep: `DisplayNode.isSmall` (`DisplayNode.kt:5`) is read in exactly one place (`MathCanvas.kt:119`, `textPaint.textSize = if (node.isSmall) 0.667f * rootFontSizePx else rootFontSizePx`) and **never set to `true` anywhere in the codebase** — its only other occurrence is the `= false` default in the constructor.
- Meanwhile `MathLayout.kt` correctly *measures* fraction numerators/denominators, exponents, and log-bases at the smaller `0.667×` size when computing box widths and the fraction-bar width.
- **Net effect:** every fraction numerator/denominator, exponent, and log-base subscript is allocated a box sized for small text, but painted at full size — guaranteed visual overflow/collision with neighboring glyphs on real output. This is not a hypothetical; it follows directly from the code, and would be visible in any screenshot of a fraction or exponent.

### C4 — All calculation happens synchronously on the UI/compose thread, including the 50,000-step numeric integration
- **Spec:** explicitly flags this as trap #1 to avoid — never let evaluation block/ANR the main thread.
- **Actual (confirmed by grep):** zero occurrences of `Dispatchers`, `withContext`, or `async` anywhere in `app/src/main/java`. `CalculatorViewModel.onKeyPressed` calls the handler synchronously from the keypad's pointer callback. The integral routine (`SubresProcessor.integrate`, 50,001 evaluations) is at least efficiently written (parses once, doesn't reparse per step), but with no background dispatch, a long/nested integral or derivative expression can still visibly freeze the UI thread.

---

## 3. High-severity findings

### H1 — `<br>` line breaks are silently dropped, collapsing multi-line complex-number output onto one line
- `NumberFormatter.formatComplex` produces strings like `"3<br>+0.25i"` for certain complex results (verified against its own test, `NumberFormatterTest.kt`, which asserts exactly this string).
- `HtmlToDisplayNode.kt` does parse `<br>` into a `DisplayNode.LineBreak` node, but I confirmed directly in `MathLayout.kt` that the `Row` layout loop (line 130-137) has **no special case for `LineBreak`** — it's just measured as a zero-width box and placed inline like any other child. There is no code anywhere that resets the horizontal cursor or advances a vertical line offset. Net effect: `"3<br>+0.25i"` renders as `3+0.25i` on one line, silently losing the line break. The same `<br>`-based strings are used in menu prompts (`MenuHandlers.kt`), so multi-line menu text is affected too.

### H2 — The integral (`∫`) construct has no rendering support at all
- `TreeBuilder.kt` emits six distinct span classes for `∫` (`integ_wrapper`, `integ_wrapper_2`, `integ_equation`, `integ_wrapper_3`, `integ_top`, `integ_bottom`) that the original's CSS uses to visually stack the bounds next to the integral sign.
- `HtmlToDisplayNode.kt`'s span-class dispatch (confirmed by reading it) has no case for any `integ_*` class — they fall through to a generic "flatten children into a Row" branch. `DisplayNode.Integral` exists as a declared type but is **never constructed** anywhere (confirmed via grep — zero hits outside its own declaration and an unreachable `else` fallback in the layout/draw code).
- **Net effect:** typing an integral produces a flat run of `∫`, the equation text, and `dX` with no visual distinction between upper/lower bounds — nothing resembling actual integral notation, even though the math *evaluates* correctly underneath.

### H3 — No cursor-following scroll; long expressions can scroll the cursor off-screen and never bring it back
- `DisplayLayer.kt`'s scroll offset is computed purely from total content width vs. viewport width, always anchored to the end of the expression — it has no dependency on where the cursor currently is.
- `CalcGeometry.kt` defines `SCROLL_X_BORDER`/`SCROLL_Y_BORDER` (extracted from the original SVG, mirroring the original's scroll-tracking DOM elements) but — confirmed by grep — these constants are **referenced nowhere else** in the app. The geometry needed to replicate cursor-following scroll was extracted from the design file but never wired to any logic.
- **Net effect:** typing a long expression and then pressing the left-arrow key to edit something in the middle leaves the cursor scrolled off-screen with no way to see it, unlike the original which recenters on the cursor after every keypress.

### H4 — The √ radical glyph never stretches to match the height of what's under it
- Original: after every render, JS measures the radicand's rendered height and applies a CSS `scaleY` transform to the `√` character so it visually wraps arbitrarily tall content (e.g. a fraction under a root).
- Kotlin `MathCanvas.kt`: draws `"√"` at a fixed font size with no `canvas.scale` call anywhere in the draw function (confirmed by reading it in full) — only the horizontal overbar line's width adapts, never the checkmark's height.
- **Net example:** `√(1/2)` — the radicand (a two-line fraction) is taller than a normal line of text; the radical sign will only visually cover the top portion, detached from the bottom of what it's supposed to enclose.

### H5 — Missing UI/instrumented tests the spec explicitly names; real coverage is much thinner than "101/101 passing" suggests
- The spec (§9.3) names three required `androidTest` files (`KeyHitTestTest`, `CalculatorScreenTest`, `SettingsTest`). Confirmed: `app/src/androidTest/java/com/my/calculator/ExampleInstrumentedTest.kt` is still the **unmodified Android Studio template stub** — none of the three exist.
- Across all unit tests, there are ~47 `@Test` methods total. Zero dedicated test files exist for `CalculatorViewModel`, `SettingsRepository`, the menu handlers (CONST/hyp/MODE/SETUP), `MathLayout`/`MathCanvas` (the entire rendering pipeline — exactly where findings C3, H1, H2, H4 above went undetected), or the keypad hit-tester.
- The spec's own instruction to keep the two `known_to_fail/*.json` golden fixtures as explicitly `@Ignore`d tests was also skipped — confirmed by grep, those files exist on disk but are referenced by no test at all, not even an ignored one.

### H6 — `isBadValue`'s CMPLX-mode error check doesn't match the spec's own stated formula (and the original)
- The spec's own pseudocode says a complex-typed result should never be treated as an error when in CMPLX mode, full stop (`complexAllowed = calcMode == CMPLX && result is Cx`, unconditionally). The original's real logic (mathjs-backed) works out to the same thing.
- Kotlin's `MathEngine.isBadValue` adds an extra `!re.isFinite() || !im.isFinite()` check inside the CMPLX branch that neither the spec's formula nor the original has. Concretely, an expression that produces a non-finite complex result (e.g. dividing by a complex zero) shows "error" in the port where both the spec and the original would display a value.

### H7 — `gamma()` uses a different, non-mathjs Lanczos coefficient table with no large-argument branch
- The original's `factorial()`/`gamma()` for non-integers is real mathjs `gamma()`, which uses a 15-term Lanczos table (g≈4.74) plus a dedicated extended-Stirling branch for arguments past 85 specifically to avoid precision loss.
- The Kotlin port uses a different, commonly-copied 8-term Lanczos table (g=7) with no large-argument branch at all.
- This is a confirmed algorithmic mismatch; the exact digit-level impact wasn't measured live (would need Node+mathjs installed), but spot-checking non-integer factorials like `5.3!` or `90.5!` against the original is the way to confirm how far the last few digits drift.

---

## 4. Medium-severity findings

### M1 — Pressing Up/Down on a fully-emptied equation line doesn't fall back to browsing history
- Original: if you type something and delete it all the way back to an empty input, pressing Up/Down discards the blank line and jumps straight into browsing previous equations (oldest for Down, most recent for Up).
- Kotlin: the same keypress just does nothing — confirmed by reading `TreeBuilder.kt`'s `key_dir1`/`key_dir3` case, there's no check for "cursor at Start with empty history" anywhere in it or in `EquationInputHandler.kt`.

### M2 — Decimal separator locale is never auto-detected on first install
- Spec: on first run, the default decimal-format setting should come from the device's actual locale.
- Actual: `SettingsRepository.kt` defaults to a literal sentinel string `"locale"`, but nothing anywhere resolves that sentinel against `Locale.getDefault()` (confirmed by grep — zero occurrences). Every place that reads the setting treats anything other than the literal string `"comma"` as period-format, so `"locale"` silently behaves as US/point format regardless of the device's real locale until the user manually changes it in Settings.

### M3 — Superscript/subscript vertical offsets are flat guessed constants, not derived from the original's actual CSS formulas, and don't distinguish raise vs. lower
- Kotlin uses the same magnitude (`0.35 × fontSize`) symmetrically for both raising an exponent and lowering a log-base subscript.
- The original's CSS math works out to an asymmetric raise/lower (~0.445rem raise vs. ~0.222rem lower — roughly 2:1), because `vertical-align` percentages in CSS are relative to each element's own (already-shrunk) font size, not the parent's. This is a numeric analysis of the CSS, not a live pixel diff — worth confirming visually, but the formula mismatch itself is real.

### M4 — Cube-root/nth-root index reuses the plain exponent styling instead of a dedicated nth-root layout
- The index digit (e.g. the "3" in a cube root) is laid out as a generic superscript floating near the radical rather than nested into the radical's corner the way conventional math notation (and the original's flexbox layout) renders it. `DisplayNode.NthRoot` is declared but never constructed (confirmed by grep).

### M5 — Engineering-notation formatting can produce a misleadingly plausible-looking wrong exponent for zero input
- For `num == 0`, `log10(0)` is `-Infinity`; converting that to an `Int` in Kotlin clamps to `Int.MIN_VALUE` rather than propagating as an error signal, so the subsequent arithmetic on the exponent silently integer-overflows/wraps to an arbitrary value instead of visibly failing the way the original's `NaN`-based breakage does. Both are "broken" for this edge case, but the Kotlin version is broken in a way that could look like a real (wrong) answer rather than an obvious error.

### M6 — Keypad-derived spec items not independently verified (needs manual/visual check, not a defect claim)
- Vertical alignment of fraction bars and radical/integral flexbox centering depend on real browser box-model behavior that's hard to fully reverse-engineer from CSS alone. Recommend screenshotting a representative expression (e.g. `1/2 + √(3/4) + x²`) side-by-side in the original (in a browser) and the Android app once the above rendering issues are addressed, to catch anything a code read alone can't.

---

## 5. Low-severity findings

- **`roundSignificant(0, places)`** silently returns `0` in Kotlin; the original's real formula produces `NaN` (→ error) for exactly zero, due to `log10(0) = -Infinity`. Narrow trigger (an integral/derivative landing on exactly `0.0`), but a real, verified divergence — possibly an intentional/reasonable improvement rather than an oversight, worth a conscious decision either way.
- **`[expr][N]` matrix-index value is parsed but never validated** — any index other than `1` is silently accepted and ignored. Not reachable through normal keypad input today, but a real fidelity gap against the original's real matrix-indexing semantics.
- **`ComplexMath.pow(0, negative)`** returns `0` instead of diverging to infinity; only reachable via an expression that constructs a literal complex zero, so narrow in practice.
- **Continued-fraction conversion** has extra iteration/remainder safety guards not present in the original — probably a reasonable defensive addition, but it is a genuine (tiny) precision divergence in pathological edge cases, worth noting rather than silently carrying.
- **`README.md` and `KNOWN_ISSUES.md`** are both missing, despite the spec asking for a `README.md` and, if anything was left incomplete, a `KNOWN_ISSUES.md` documenting it. Given the gaps found in this report and zero `TODO` comments anywhere in the codebase, the build agent appears to have declared full completion rather than leaving the paper trail the spec asked for when something is incomplete.
- **Italic-font load-failure fallback** uses `Typeface.DEFAULT_BOLD` instead of an actual italic fallback — cosmetic, only matters if the bundled font file itself fails to load.

---

## 6. Areas independently verified as faithful — not corner-cut

To be fair to the work that *is* solid (and so you don't waste time re-checking these):

- **Core math engine**: full function set, operator precedence (including unary-minus-before-power), sexagesimal (°) parsing (10-rule regex chain matches the spec verbatim), the `subres` mechanism backing integrals/derivatives (parses once, evaluates the AST 50,001 times — matches the spec's performance requirement), and the physical-constants table (all 43 values, correct indices) are all faithful, line-for-line-verified ports.
- **Note on scope**: the original app itself has no nPr/nCr/GCD/LCM/Pol-Rec/base-N/statistics/matrices/random functions at all — it delegates general math to the mathjs library and only adds ~9 custom helpers on top. So the Kotlin port not having those either is correct scope, not a missing feature.
- **Keypad hit-testing**: all 50 keys, correct reverse-document-order polygon hit-testing, matches the spec's algorithm exactly — this was the area most expected to have shortcuts, and it doesn't.
- **Indicators**: all 18 indicator drawables exist; the ones the original actually drives are wired to real state, and the ones the original never uses are correctly left inert (not a bug — matches spec).
- **Menus (MODE/SETUP/CONST/hyp)**: genuinely wired into the UI and driving real display state, not dead/disconnected code.
- **SHIFT/ALPHA/STO/M+/M- input handling**, cursor navigation, container deletion, and the equation-history parent handler: all verified line-for-line equivalent to the original, aside from finding M1 above.
- **Licensing/attribution**: `NOTICE.md`/`LICENSE`/the in-app Licenses screen correctly preserve the original author's (Joris Yidong Scholl) CC BY-SA 4.0 / GPL-3.0 attribution alongside the MK Shaon branding — no license-violation shortcuts found.
- **Permissions**: `AndroidManifest.xml` requests zero permissions, as intended.

---

## 7. Suggested priority order if you want fixes

1. **C2** (Turn Off doesn't close the app) — small, self-contained, on the spec's hard acceptance list.
2. **C3 + H1 + H4** (isSmall sizing, `<br>` line breaks, √ scaling) — all three live in the same rendering pipeline and are probably fastest to fix together once you're in that code.
3. **H2** (integral rendering) — bigger effort, but currently integrals are mathematically correct yet visually broken.
4. **H3** (cursor-following scroll) — the geometry constants already exist, just need wiring.
5. **H6 + H7** (CMPLX error-detection formula, gamma table) — narrow-but-real numerical correctness fixes.
6. **M1, M2** — small, self-contained.
7. **C1** (HTML-string architecture) — largest, most invasive change; consider whether it's worth it now that the concrete bugs it enabled (C3/H1/H2/H4) are known and independently fixable, versus a full rewrite of the render pass per the original spec.
8. **H5** — write the missing tests, ideally *before* fixing the rendering bugs above, so you have regression coverage going forward.

---

*This report reflects a source-code-level audit as of the commits currently on `main` (`3f86668` and earlier). It does not replace visually running the app side-by-side with the original for the items flagged "needs manual verification."*

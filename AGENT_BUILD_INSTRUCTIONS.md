# MyCalculator — ফুল নেটিভ Android ক্যালকুলেটর (Agent Build Instructions)

> **এই ফাইলটা কে পড়বে:** Google Antigravity-র AI কোডিং এজেন্ট (বা যেকোনো Android জুনিয়র ডেভেলপার)।
> **কাজ:** `schulrechner` নামের একটা ওয়েব-বেসড (HTML/CSS/JS + Cordova) স্কুল-ক্যালকুলেটর অ্যাপকে
> **১০০% নেটিভ Android (Kotlin + Jetpack Compose)** অ্যাপে রূপান্তর করা — একই UI, একই UX, একই গণিত-লজিক,
> কিন্তু **সম্পূর্ণ নতুন করে লেখা কোড**।
>
> **এই ডকুমেন্টটাই তোমার একমাত্র স্পেসিফিকেশন।** নিচের Phase 0 → Phase 10 ক্রমানুসারে কাজ করো।
> কোনো ফেজ "আংশিক" রেখে পরেরটায় যাবে না। প্রতিটা ফেজের শেষে **Definition of Done (DoD)** আছে —
> সেটা পাস না করা পর্যন্ত ওই ফেজ শেষ নয়।

---

## 0. TL;DR — মাস্টার চেকলিস্ট

```
[ ] Phase 0  — প্রস্তুতি, রেপো পড়া, গ্রাউন্ড রুল বোঝা
[ ] Phase 1  — Gradle / Manifest / Theme / প্যাকেজ স্ট্রাকচার সেটআপ
[ ] Phase 2  — অ্যাসেট পাইপলাইন: SVG → VectorDrawable + Geometry কোড জেনারেশন, ফন্ট বান্ডল
[ ] Phase 3  — Core Math Engine (Kotlin এক্সপ্রেশন পার্সার + ইভ্যালুয়েটর + কমপ্লেক্স নাম্বার)
[ ] Phase 4  — Input Model (গণিত-এলিমেন্ট ট্রি, কার্সর, নেইবার গ্রাফ)
[ ] Phase 5  — Input Handler স্টেট মেশিন (shift/alpha/STO, মেনু, হিস্ট্রি)
[ ] Phase 6  — Number Formatting (ভগ্নাংশ, Fix/Norm, ENG, sexagesimal, complex)
[ ] Phase 7  — Math Display Renderer (Compose-এ গণিত টাইপসেটিং + স্ক্রলিং)
[ ] Phase 8  — UI Shell (কীপ্যাড হিট-টেস্ট, ইন্ডিকেটর, সেটিংস, About)
[ ] Phase 9  — টেস্ট (১০০টা পোর্ট করা ইউনিট টেস্ট + UI টেস্ট + ম্যানুয়াল চেকলিস্ট)
[ ] Phase 10 — বিল্ড, ডিভাইস ভেরিফিকেশন, পলিশ, ফাইনাল একসেপ্টেন্স
```

---

## 1. পাথ ও রেফারেন্স

| জিনিস | পাথ |
|---|---|
| **সোর্স (রেফারেন্স, READ-ONLY)** | `/Users/mkshaon/playground/schulrechner` |
| **টার্গেট (এখানে কাজ হবে)** | `/Users/mkshaon/playground/MyCalculator` |
| অ্যাপ নাম | `My Calculator` |
| প্যাকেজ / applicationId | `com.my.calculator` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |

### সোর্স রেপোর যে ফাইলগুলো আসল (এগুলো ভালো করে পড়ো)

| ফাইল | লাইন | কী আছে |
|---|---|---|
| `www/js/logic.js` | 2183 | **পুরো গণিত-লজিক**: এলিমেন্ট ক্লাস, ইনপুট হ্যান্ডলার, ফরম্যাটিং |
| `www/js/main.js` | 689 | UI লেয়ার: SVG লোড, ইভেন্ট, সেটিংস, localStorage, কী-বোর্ড |
| `www/css/styles.css` | 461 | ডিসপ্লে টাইপসেটিং (frac/sqrt/pow/integ) + সেটিংস স্টাইল |
| `www/index.html` | 90 | DOM স্ট্রাকচার |
| `www/img/gui/Classic_by_Joris Yidong Scholl.svg` | 178 KB | **পুরো ক্যালকুলেটরের আর্টওয়ার্ক** (viewBox `0 0 2486.6667 4912`) |
| `www/img/gui/Classic_by_Joris Yidong Scholl.json` | 47 | **mode_maps** (shift/alpha/STO/cmplx কী-ম্যাপ) + font color |
| `www/fonts/Schulrechner-Regular.ttf` / `-Italic.ttf` | — | LCD ডিসপ্লের কাস্টম ফন্ট (79 গ্লিফ, PUA `U+E000` = কার্সর) |
| `tests/input_to_output_jsons/test_batch_1.json` | 100 কেস | **গোল্ডেন টেস্ট ডেটা** — এগুলো পোর্ট করতেই হবে |
| `tests/input_to_output_jsons/fraction_2.json` | 1 কেস | আরেকটা টেস্ট |
| `tests/input_to_output_jsons/known_to_fail/*.json` | 2 কেস | জানা বাগ — এগুলো `@Ignore` করে রাখবে |
| `www/img/phoneScreenshots/1..3.jpg` | — | **চেহারা কেমন হবে তার রেফারেন্স ছবি** |

---

## 2. অপরিবর্তনীয় নিয়ম (Hard Rules)

1. **কোনো WebView নেই।** কোনো HTML, কোনো CSS, কোনো JavaScript, কোনো Cordova — একটাও না।
   পুরোটা Kotlin + Jetpack Compose। `android.webkit.*` ইমপোর্ট করলেই কাজ ফেল।
2. **কোড লাইন-বাই-লাইন ট্রান্সলিটারেট করবে না।** JS ফাইল পড়ে *আচরণ* (behaviour) বুঝবে, তারপর
   **ইডিওম্যাটিক Kotlin**-এ নতুন করে লিখবে:
   - `class Math_Element` → `sealed interface MathNode` / `sealed class` + data class
   - `neighbors[0..3]` অ্যারে → নামযুক্ত প্রপার্টি (`left`, `down`, `right`, `up`)
   - `switch(input_code)` এর বিশাল চেইন → `when` + একটা `KeyAction` ম্যাপিং টেবিল
   - `global_logic_vars` গ্লোবাল অবজেক্ট → `CalculatorViewModel` + `StateFlow<CalculatorUiState>`
   - snake_case → camelCase, JS-এর mutable hack → Kotlin immutable data + `copy()`
   - JSDoc/জার্মান কমেন্ট কপি করবে না; নিজের ইংরেজি KDoc লিখবে।
3. **ডেড কোড পোর্ট করবে না।** সোর্সে যেগুলো অব্যবহৃত: `InputHandler.round_to_significant_places`,
   `Start_Element.prio`, `Sqrtn_Element`/`Derivate_Element`-এর অব্যবহৃত `subres_id` ভেরিয়েবল,
   `key_calc` (কোনো হ্যান্ডলার নেই), `key_rcl` নিজে (শুধু shift+rcl = STO কাজ করে)।
   `key_calc` বাটনটা দেখা যাবে কিন্তু চাপলে কিছুই হবে না — **এই আচরণটাই রাখবে**।
4. **UI/UX হুবহু এক।** বাটনের জায়গা, সাইজ, রঙ, প্রেস-অ্যানিমেশন, ডিসপ্লের লেআউট, স্ক্রলিং —
   সব মূল অ্যাপের মতো। শুধু নিচের ৩ নম্বর জিনিসগুলো বদলাবে।
5. **যেগুলো বদলাবে (এবং শুধু এগুলোই):**
   - অ্যাপের নাম: `Schulrechner` → `My Calculator`
   - প্যাকেজ: `io.cardijey.schulrechner` → `com.my.calculator`
   - সেটিংসের সব লিংক/ডোনেট বাটন → শুধু নিচের তথ্য (Phase 8 দেখো):
     **MK Shaon** · `https://github.com/mkshaonexe` · `https://mkshaon.com` · `mkshaondev@gmail.com`
   - মূল লেখকের changelog/F-Droid/Liberapay/Flathub সংক্রান্ত সব কিছু বাদ।
6. **INTERNET পারমিশন নেই।** মূল অ্যাপ ইচ্ছে করে `INTERNET` পারমিশন সরিয়ে দেয়
   (`config.xml` → `AndroidRemovePermissions = INTERNET`)। আমরাও দেব না।
   লিংক খুলবে `Intent.ACTION_VIEW` দিয়ে (এতে পারমিশন লাগে না)।
7. **সব কিছু অফলাইন।** কোনো নেটওয়ার্ক কল, কোনো অ্যানালিটিক্স, কোনো ট্র্যাকিং, কোনো অ্যাড।
8. **অ্যাসেট ব্যবহার (ইউজারের সিদ্ধান্ত):** মূল SVG আর্টওয়ার্ক ও TTF ফন্ট সরাসরি বান্ডল করা হবে
   (VectorDrawable + `res/font`)। Phase 2 দেখো। **কিন্তু কোড নতুন করেই লেখা হবে।**

---

## 3. লাইসেন্স ও অ্যাট্রিবিউশন (এটা বাদ দেওয়া যাবে না)

এই তথ্যটা ফ্যাক্ট, মানতেই হবে:

- মূল প্রোজেক্ট `schulrechner` — **GPL-3.0-only**, © Joris Yidong Scholl।
  এটার আচরণ/লজিক থেকে বানানো অ্যাপ আইনত derivative work, তাই **অ্যাপটাও GPL-3.0 রাখতে হবে**
  (সোর্স কোড শেয়ার করার বাধ্যবাধকতাসহ)।
- `Classic_by_Joris Yidong Scholl.svg` আর্টওয়ার্ক — **CC BY-SA 4.0**, © Joris Yidong Scholl।
  এটা বান্ডল করলে **নাম উল্লেখ (Attribution) + একই লাইসেন্সে শেয়ার (ShareAlike)** বাধ্যতামূলক।
- ফন্ট `Schulrechner-Regular/Italic.ttf` — © Joris Scholl, প্রোজেক্টের GPL-3.0-র অধীনে।

**তাই অ্যাপে একটা "Licenses / Open Source" স্ক্রিন বানাতেই হবে** (Phase 8, Task 8.7) যেখানে থাকবে:
```
Calculator artwork & display font
  © Joris Yidong Scholl — CC BY-SA 4.0 / GPL-3.0
  https://github.com/CardiJey/schulrechner

This app is licensed under GPL-3.0-only.
```
একই সাথে রিপোর রুটে `LICENSE` (GPL-3.0 ফুল টেক্সট) আর `NOTICE.md` ফাইল রাখবে।

---

## 4. আর্কিটেকচার ওভারভিউ

### 4.1 ডেটা-ফ্লো (মূল অ্যাপের ফ্লো-টাই রাখা হচ্ছে, কারণ এতে edge-case আচরণ হুবহু মেলে)

```
কী চাপ (pointerDown)
   → KeyCode  (যেমন KeyCode.SEVEN, KeyCode.FRAC)
   → InputHandler.handle(keyCode)            [স্টেট মেশিন: shift/alpha/STO রিম্যাপ, মেনু, ইত্যাদি]
   → inputCodeHistory: List<KeyCode>          [প্রতিবার পুরো হিস্ট্রি রি-প্লে করা হয় — এটাই মূল ডিজাইন]
   → buildElementTree(history)                [MathNode গ্রাফ + কার্সর পজিশন]
   → renderTree()  →  DisplayNode (ডিসপ্লে)   ┐
   → buildExpression() → String (গণিত এক্সপ্রেশন) ┘
   → MathEngine.evaluate(expr, scope)         [শুধু "=" চাপলে]
   → NumberFormatter.format(result, mode)
   → Compose UI (MathCanvas) রেন্ডার
```

> ⚠️ **গুরুত্বপূর্ণ:** মূল অ্যাপ প্রতিবার কী-প্রেসে **পুরো `input_code_history` শূন্য থেকে রি-প্লে** করে
> ট্রি বানায়। এটা ইচ্ছাকৃত এবং অনেক edge-case (কার্সর মুভমেন্ট, ডিলিট, ব্লক-ইনক্লুশন) এর ওপর নির্ভরশীল।
> **এই ডিজাইনটা বদলাবে না** — ইনক্রিমেন্টাল ট্রি আপডেট করতে গেলে টেস্ট ফেল করবে।

### 4.2 প্যাকেজ স্ট্রাকচার (এটাই বানাও)

```
app/src/main/java/com/my/calculator/
├── MainActivity.kt
├── CalculatorApp.kt                     # রুট Composable
├── core/
│   ├── math/
│   │   ├── MathValue.kt                 # sealed: RealValue / ComplexValue
│   │   ├── Complex.kt                   # কমপ্লেক্স অ্যারিথমেটিক
│   │   ├── Tokenizer.kt
│   │   ├── Parser.kt                    # recursive-descent → Ast
│   │   ├── Ast.kt
│   │   ├── Evaluator.kt
│   │   ├── MathFunctions.kt             # sin/cos/…/nthRootComplex/derivate/integrate
│   │   └── MathEngine.kt                # facade: evaluate(expr, scope): MathValue
│   ├── model/
│   │   ├── KeyCode.kt                   # ৫০টা ফিজিক্যাল কী + ভার্চুয়াল কোড
│   │   ├── MathNode.kt                  # ডিসপ্লে/এক্সপ্রেশন এলিমেন্ট
│   │   ├── NodeGraph.kt                 # neighbor গ্রাফ বিল্ডার + get_left_block সমতুল্য
│   │   └── ElementFactory.kt            # KeyCode → MathNode
│   ├── input/
│   │   ├── InputHandler.kt              # sealed base
│   │   ├── EquationInputHandler.kt
│   │   ├── EquationListHandler.kt       # মূলের EquationSelectInputHandler
│   │   ├── ComplexEquationHandler.kt
│   │   ├── MenuHandlers.kt              # Const / Hyp / Mode / Setup / TurnedOff / Void
│   │   └── ModeMaps.kt                  # shift/alpha/STO/cmplx ম্যাপ
│   └── format/
│       ├── NumberFormatter.kt
│       ├── ContinuedFraction.kt
│       └── DecimalSeparator.kt
├── display/
│   ├── DisplayNode.kt                   # টাইপসেট ট্রি (Text/Frac/Sqrt/Sup/Sub/Integ/Cursor/Row)
│   ├── MathLayout.kt                    # measure পাস
│   ├── MathCanvas.kt                    # draw পাস (Compose Canvas + TextMeasurer)
│   └── DisplayMetrics.kt                # rem→px স্কেলিং
├── ui/
│   ├── CalculatorScreen.kt
│   ├── KeypadLayer.kt                   # হিট-টেস্ট + প্রেস অ্যানিমেশন
│   ├── IndicatorLayer.kt
│   ├── SettingsSheet.kt
│   ├── AboutSheet.kt
│   └── theme/ (Color.kt, Theme.kt, Type.kt)
├── data/
│   └── SettingsRepository.kt            # DataStore (localStorage-এর বিকল্প)
└── generated/
    └── CalcGeometry.kt                  # Phase 2-এ স্ক্রিপ্ট দিয়ে অটো-জেনারেট
```

---

# Phase 0 — প্রস্তুতি

### টাস্ক

- [ ] **0.1** `/Users/mkshaon/playground/schulrechner` রেপোটা পুরো পড়ো। বিশেষ করে
      `www/js/logic.js` (পুরোটা), `www/js/main.js`, `www/css/styles.css`, ডিজাইন `.json`।
- [ ] **0.2** `www/img/phoneScreenshots/1.jpg`, `2.jpg`, `3.jpg` দেখো — টার্গেট চেহারা এটাই।
- [ ] **0.3** `tests/input_to_output_jsons/test_batch_1.json` খুলে দেখো — ১০০টা কেস আছে।
      এগুলো তোমার "সত্যের উৎস" (source of truth)।
- [ ] **0.4** Node আছে কিনা দেখে মূল টেস্ট একবার চালাও (রেফারেন্স আউটপুট বোঝার জন্য):
      ```bash
      cd /Users/mkshaon/playground/schulrechner && npm install && npm test
      ```
      (এটা ঐচ্ছিক — নেট না থাকলে স্কিপ করো, JSON ফাইলেই expected আউটপুট আছে।)
- [ ] **0.5** টার্গেট প্রোজেক্টে git init করো, প্রথম কমিট করো (খালি টেমপ্লেট স্টেট সেভ রাখতে)।
- [ ] **0.6** এই ফাইলের **Appendix A–H** একবার পড়ে নাও — ওখানে সব টেবিল-ডেটা আছে।

### DoD
- তুমি মুখে মুখে বলতে পারবে: কীভাবে `key_frac` চাপলে আগের ব্লকটা ভগ্নাংশের ওপরে চলে যায়,
  আর `subres` মেকানিজমটা কী কাজ করে।

---

# Phase 1 — প্রোজেক্ট সেটআপ

বর্তমান অবস্থা: `/Users/mkshaon/playground/MyCalculator` একটা খালি Android Studio Compose টেমপ্লেট
(AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, `com.my.calculator`)।

### টাস্ক

- [ ] **1.1 `gradle/libs.versions.toml`** — এই ডিপেন্ডেন্সিগুলো যোগ করো:
  ```toml
  [versions]
  lifecycleViewmodelCompose = "2.9.4"
  datastore = "1.1.7"
  coreSplashscreen = "1.0.1"
  kotlinxCoroutines = "1.10.2"

  [libraries]
  androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
  androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
  androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "coreSplashscreen" }
  kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
  ```
  > ভার্সন রেজোলিউশনে সমস্যা হলে লেটেস্ট স্টেবল নাও। **কোনো থার্ড-পার্টি math/SVG লাইব্রেরি নয়** —
  > গণিত ইঞ্জিন নিজে লিখতে হবে (Phase 3), আর SVG বিল্ড-টাইমে VectorDrawable-এ কনভার্ট হবে (Phase 2)।

- [ ] **1.2 `app/build.gradle.kts`** —
  - উপরের লাইব্রেরিগুলো `implementation`-এ যোগ করো।
  - `testImplementation(libs.junit)` আছে; `testImplementation(kotlin("test"))` ও যোগ করো।
  - `android { sourceSets["main"].assets.srcDirs(...) }` লাগবে না (ফন্ট `res/font`-এ যাবে)।
  - `buildTypes.release`-এ `isMinifyEnabled = true` করো এবং `proguard-rules.pro`-তে কিছু লাগবে না
    (রিফ্লেকশন ব্যবহার করছি না)।
  - `kotlinOptions`/`compilerOptions`-এ `jvmTarget = "11"` (compileOptions-এর সাথে মিল রাখো)।

- [ ] **1.3 `AndroidManifest.xml`** —
  - **কোনো `<uses-permission>` নয়।** একদম শূন্য।
  - `<application android:label="@string/app_name" android:theme="@style/Theme.MyCalculator" ...>`
  - `<activity android:name=".MainActivity"`
    `android:screenOrientation="portrait"`
    `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|uiMode"`
    `android:exported="true"` + LAUNCHER intent-filter।
  - `android:allowBackup="true"`, `android:fullBackupContent="@xml/backup_rules"`।

- [ ] **1.4 থিম / ফুলস্ক্রিন** — মূল অ্যাপ `Fullscreen = true`।
  - `res/values/themes.xml`-এ `Theme.MyCalculator` বানাও: `parent="android:Theme.Material.NoActionBar"`
    (অথবা Compose-এর জন্য `Theme.SplashScreen`), `android:windowBackground = #000000`।
  - `MainActivity.onCreate`-এ:
    ```kotlin
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    ```
  - স্ক্রিন সবসময় অন রাখার দরকার নেই (মূল অ্যাপেও নেই)।

- [ ] **1.5 `res/values/strings.xml`** — `<string name="app_name">My Calculator</string>` + বাকি সব
      ইউজার-দৃশ্যমান টেক্সট এখানে (হার্ডকোড করবে না)।

- [ ] **1.6 অ্যাপ আইকন** — মূল রেপোর `res/icon/android/{foreground,background,monochrome}.svg`
      থেকে adaptive icon বানাও (`mipmap-anydpi-v26/ic_launcher.xml` + `drawable/ic_launcher_*.xml`)।
      স্প্ল্যাশ ব্যাকগ্রাউন্ড `#000000` (মূলের `SplashScreenBackgroundColor`)।

- [ ] **1.7** প্যাকেজ ফোল্ডারগুলো (§4.2) খালি করে বানিয়ে রাখো।

- [ ] **1.8** টেমপ্লেটের `Greeting`/`GreetingPreview` কোড মুছে দাও।

### DoD
```bash
cd /Users/mkshaon/playground/MyCalculator && ./gradlew :app:assembleDebug
```
- বিল্ড সফল, অ্যাপ ইনস্টল হয়, কালো ফুলস্ক্রিন খালি স্ক্রিন দেখায়, কোনো স্ট্যাটাস/নেভ বার নেই।
- `aapt dump permissions` চালালে কোনো পারমিশন আসে না।

---

# Phase 2 — অ্যাসেট পাইপলাইন (SVG → VectorDrawable + Geometry)

এটাই সবচেয়ে "মেকানিক্যাল" ফেজ, কিন্তু ভুল হলে পুরো UI বিগড়ে যাবে। ধৈর্য ধরো।

### 2.0 বোঝাপড়া — SVG-টা কীভাবে বানানো

`Classic_by_Joris Yidong Scholl.svg`:
- `viewBox="0 0 2486.6667 4912"` (aspect ratio ≈ **0.50625 : 1**, অর্থাৎ লম্বা পোর্ট্রেট)
- সব কিছু একটা গ্রুপে: `<g id="layer1" transform="translate(-1.6006437, 0.31985911)">`
  → **এই ট্রান্সফর্মটা বেক (bake) করতে হবে**, নাহলে সব ১.৬ ইউনিট সরে যাবে।
- এলিমেন্টের ৪ রকম ভূমিকা:

| আইডি প্যাটার্ন | সংখ্যা | ভূমিকা | Android-এ কী হবে |
|---|---|---|---|
| `key_*` | **50** | `opacity:0` অদৃশ্য **হিট-এরিয়া পলিগন** (fill `#ff00c9`) | রেন্ডার হবে **না**; শুধু হিট-টেস্টের জন্য pathData কোডে যাবে |
| `label_background_*` | **50** | প্রতিটা বাটনের **দৃশ্যমান ব্যাকগ্রাউন্ড** (একটা করে `<path>`) | আলাদা VectorDrawable — প্রেস হলে ডার্ক করা হবে |
| `indicator_*` | **18** | LCD-র উপরের স্ট্যাটাস আইকন, ডিফল্টে হিডেন | আলাদা VectorDrawable — শর্তসাপেক্ষে দেখানো |
| `display_input`, `display_output`, `scroll_x_border`, `scroll_y_border` | 4 | `opacity:0` রেফারেন্স `<rect>` | রেন্ডার হবে না; শুধু জ্যামিতি |
| বাকি সব | ~2800 path | কেসিং, সোলার প্যানেল, LCD প্যানেল, বাটনের লেখা | এক বড় VectorDrawable = **body** |
| `label_comma_de_DE`, `label_comma_en_US` | 2 | `display:none`, লোকেল অনুযায়ী দেখানো হয় | আলাদা VectorDrawable |

### 2.1 জেনারেটর স্ক্রিপ্ট লেখো

`tools/generate_assets.py` বানাও (Python 3, শুধু stdlib — `xml.etree.ElementTree` + `re`)।

**ইনপুট:** `/Users/mkshaon/playground/schulrechner/www/img/gui/Classic_by_Joris Yidong Scholl.svg`

**আউটপুট:**

| ফাইল | কী থাকবে |
|---|---|
| `app/src/main/res/drawable/calc_body.xml` | `key_*`, `label_background_*`, `indicator_*`, `display_*`, `scroll_*`, `label_comma_*` **বাদে** বাকি সব path |
| `app/src/main/res/drawable/keybg_<code>.xml` × 50 | প্রতিটা `label_background_<code>` |
| `app/src/main/res/drawable/ind_<name>.xml` × 18 | প্রতিটা `indicator_<name>` (গ্রুপ হলে ভিতরের সব path সহ, ট্রান্সফর্মসহ) |
| `app/src/main/res/drawable/label_comma_de.xml`, `label_comma_en.xml` | কমার লোকেল-লেবেল (`display:none` সরিয়ে) |
| `app/src/main/java/com/my/calculator/generated/CalcGeometry.kt` | নিচে দেখো |

**প্রতিটা VectorDrawable-এর হেডার একই রাখবে** (এতে সব ওভারলে নিখুঁতভাবে একটার ওপর আরেকটা বসবে):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="248.67dp" android:height="491.2dp"
    android:viewportWidth="2486.6667" android:viewportHeight="4912">
    <group android:translateX="-1.6006437" android:translateY="0.31985911">
        ... paths ...
    </group>
</vector>
```

**স্টাইল ম্যাপিং রুল:**
- `style="fill:#RRGGBB"` → `android:fillColor="#RRGGBB"`
- `fill-opacity:X` → `android:fillAlpha="X"`
- `opacity:X` (এলিমেন্ট লেভেল) → `android:fillAlpha` এর সাথে গুণ করে দাও
- `fill:none` → `android:fillColor="@android:color/transparent"` (বা path বাদ)
- `stroke:#RRGGBB` + `stroke-width:W` → `android:strokeColor` + `android:strokeWidth`
- `stroke-dasharray` → **VectorDrawable সাপোর্ট করে না** — উপেক্ষা করো
  (যেসব path-এ এটা আছে সেগুলোর `opacity:0`, তাই কোনো ভিজ্যুয়াল পার্থক্য হবে না)
- `display:none` → body থেকে বাদ (কিন্তু `label_comma_*` আলাদা ফাইলে যাবে, সেখানে দেখাবে)
- `<linearGradient>` → `aapt:attr` দিয়ে ইনলাইন:
  ```xml
  <path android:pathData="...">
    <aapt:attr name="android:fillColor">
      <gradient android:type="linear"
          android:startX="1260.5577" android:startY="4925.0430"
          android:endX="1193.5947" android:endY="-7.2104"
          >   <!-- gradientTransform translate(-5.065071,-9.6786949) বেক করা -->
        <item android:offset="0.48792869" android:color="#777579"/>
        <item android:offset="1" android:color="#cfced3"/>
      </gradient>
    </aapt:attr>
  </path>
  ```
  > SVG-তে একটাই gradient আছে: `linearGradient36525` (`#777579` @0.488 → `#cfced3` @1),
  > `gradientUnits="userSpaceOnUse"`, `gradientTransform="translate(-5.065071,-9.6786949)"`।
  > স্ক্রিপ্টে gradientTransform-টা start/end কোঅর্ডিনেটে যোগ করে দাও।
- `<rect>`, `<circle>`, `<ellipse>` → path data-তে কনভার্ট করো (VectorDrawable শুধু path চেনে)।
- `<use xlink:href>` থাকলে রিজলভ করে ইনলাইন করো।

- [ ] **2.2 `CalcGeometry.kt` জেনারেট করো** — এই শেপে:
```kotlin
package com.my.calculator.generated

/** SVG viewBox. সব কোঅর্ডিনেট এই স্পেসে। */
const val CALC_VIEWPORT_WIDTH = 2486.6667f
const val CALC_VIEWPORT_HEIGHT = 4912f

data class KeyArea(
    val code: String,          // "key_7"
    val pathData: String,      // SVG path "d" (layer1 transform বেক করা)
    val left: Float, val top: Float, val right: Float, val bottom: Float // bbox
)

/** ⚠️ SVG ডকুমেন্ট-অর্ডারে। হিট-টেস্ট করতে হবে **উল্টো দিক থেকে** (শেষেরটা সবার উপরে)। */
val KEY_AREAS: List<KeyArea> = listOf(/* ... ৫০টা ... */)

data class RectSpec(val left: Float, val top: Float, val width: Float, val height: Float)
val DISPLAY_INPUT  = RectSpec(331.4671f,  886.10553f, 1821.9519f, 590.78076f)
val DISPLAY_OUTPUT = RectSpec(331.46701f, 914.86511f, 1821.9519f, 580f)
val SCROLL_X_BORDER = RectSpec(1949.8523f, 888.66815f, 15.884805f, 188.69521f)
val SCROLL_Y_BORDER = RectSpec(338.09949f, 1442.5986f, 356.84247f, 2.7682812f)

/** key code → keybg drawable res id ম্যাপ (R ক্লাস রেফারেন্স) */
val KEY_BACKGROUND_DRAWABLES: Map<String, Int> = mapOf(/* ... */)
val INDICATOR_DRAWABLES: Map<String, Int> = mapOf(/* ... */)
```
> `RectSpec` ভ্যালুগুলোতেও `layer1` ট্রান্সফর্ম যোগ করে দিও (`left - 1.6006437`, `top + 0.31985911`)।

- [ ] **2.3 ফন্ট বান্ডল করো**
  - `www/fonts/Schulrechner-Regular.ttf` → `app/src/main/res/font/schulrechner_regular.ttf`
  - `www/fonts/Schulrechner-Italic.ttf` → `app/src/main/res/font/schulrechner_italic.ttf`
  - `app/src/main/res/font/schulrechner.xml` (font family):
    ```xml
    <font-family xmlns:android="http://schemas.android.com/apk/res/android">
        <font android:fontStyle="normal" android:fontWeight="400" android:font="@font/schulrechner_regular"/>
        <font android:fontStyle="italic" android:fontWeight="400" android:font="@font/schulrechner_italic"/>
    </font-family>
    ```
  - Compose-এ: `val SchulrechnerFont = FontFamily(Font(R.font.schulrechner_regular), Font(R.font.schulrechner_italic, style = FontStyle.Italic))`
  - **ফন্টে থাকা বিশেষ গ্লিফ** (এগুলোই ডিসপ্লেতে ব্যবহার হবে):

    | কোডপয়েন্ট | দেখতে | ব্যবহার |
    |---|---|---|
    | `U+E000` | কার্সর ব্লক | ব্লিংকিং কার্সর |
    | `U+25AF` `▯` | খালি বাক্স | কন্টেইনার প্লেসহোল্ডার |
    | `U+2491` `⒑` | ছোট "10" | `×⒑` (×10ˣ) |
    | `U+2192` `→` | তীর | STO (`→A`) |
    | `U+221A` `√` | রুট | বর্গমূল (উচ্চতা স্কেল করা হয়) |
    | `U+00B0 U+2032 U+2034` `° ′ ‴` | ডিগ্রি/মিনিট/সেকেন্ড | sexagesimal |

- [ ] **2.4 রঙের প্যালেট** `ui/theme/Color.kt`-এ (Appendix E দেখো)।

### DoD
- [ ] ৫০টা `keybg_*.xml`, ১৮টা `ind_*.xml`, ২টা `label_comma_*.xml`, ১টা `calc_body.xml` তৈরি হয়েছে।
- [ ] একটা টেস্ট Composable বানাও যেটা `calc_body` + সব `keybg_*` একসাথে ওভারলে করে দেখায় —
      এটা `phoneScreenshots/2.jpg`-এর ক্যালকুলেটরটার মতো দেখতে হবে (লেখা, বাটন, LCD সব ঠিক জায়গায়)।
- [ ] `CalcGeometry.KEY_AREAS.size == 50`
- [ ] বিল্ড সফল, কোনো `aapt` ওয়ার্নিং নেই।

### ⚠️ ফলব্যাক (যদি SVG→VectorDrawable কনভার্শন আটকে যায়)
কোনো path কনভার্ট না হলে **হাল ছেড়ে দিও না**:
1. আগে দেখো কোন এলিমেন্ট ফেল করছে (স্ক্রিপ্টে লগ দাও)।
2. `<rect>/<circle>` → path কনভার্শন ঠিক আছে কিনা দেখো।
3. খুব বেশি path হলে (`calc_body` যদি রেন্ডার স্লো হয়) — body-টাকে ২-৩টা ড্রয়েবলে ভাগ করো
   (background / labels / details), একটার ওপর একটা বসাও। সব একই viewport, তাই অ্যালাইনমেন্ট নষ্ট হবে না।

---

# Phase 3 — Core Math Engine (Kotlin)

মূল অ্যাপ [math.js](https://mathjs.org) ব্যবহার করে। আমরা **নিজেরা** একটা ছোট কিন্তু নিখুঁত
এক্সপ্রেশন ইঞ্জিন লিখব — কারণ (ক) নেটিভ হতে হবে, (খ) কোনো JS নির্ভরতা রাখা যাবে না।

> এটাই সবচেয়ে ঝুঁকিপূর্ণ ফেজ। **Phase 9-এর ১০০টা টেস্ট এই ইঞ্জিনের ওপরই দাঁড়াবে।**
> তাই এই ফেজের টেস্ট আগে লিখো (TDD), পরে ইমপ্লিমেন্ট করো।

### 3.1 এক্সপ্রেশন ভাষাটা ঠিক কী? (ইনপুট মডেল যা তৈরি করে)

এলিমেন্ট ট্রি থেকে যে স্ট্রিং তৈরি হয় তাতে **শুধু নিচের জিনিসগুলোই** থাকে:

| গঠন | উদাহরণ | মানে |
|---|---|---|
| সংখ্যা | `150238`, `4.5`, `5e3`, `1.672621637e-27` | দশমিক লিটারেল; `e` = ১০-এর ঘাত |
| বাইনারি অপারেটর | `+ - * / ^` | যোগ/বিয়োগ/গুণ/ভাগ/ঘাত |
| ইউনারি | `-5` | ঋণাত্মক |
| পোস্টফিক্স | `5!` | ফ্যাক্টোরিয়াল |
| বন্ধনী | `(...)` | গ্রুপিং |
| **ম্যাট্রিক্স গ্রুপ + ইনডেক্স** | `[expr][1]` | **গ্রুপিং ট্রিক** — ১-এলিমেন্টের ম্যাট্রিক্স, ১-ভিত্তিক ইনডেক্স |
| ইমপ্লিসিট গুণ | `12(3.14159)`, `5(3-7)`, `8log10(4)` | পাশাপাশি দুটো অপারেন্ড = গুণ |
| ফাংশন কল | `sin(x)`, `log10(x)`, `nthRootComplex(n,x)` | নিচের তালিকা |
| ভেরিয়েবল | `X` | একমাত্র সিম্বলিক ভেরিয়েবল (scope থেকে আসে) |
| কমপ্লেক্স ইউনিট | `i` | √-1 |
| ধ্রুবক | `PI` | π (শুধু asin/acos/atan-এ `180/PI*` আকারে) |
| `error` | `error` | ইচ্ছাকৃত অবৈধ টোকেন → ইভ্যালুয়েশন ফেল → "error" দেখাবে |
| `subres…` মার্কার | `subres3idstart` ইত্যাদি | ইভ্যালুয়েশনের **আগেই** প্রি-প্রসেস হয়ে যায় (§3.5) |

> **`[expr][1]` কেন?** math.js-এ এটা এক-এলিমেন্টের ম্যাট্রিক্স বানিয়ে প্রথম এলিমেন্ট নেওয়া —
> মানে জোর করে গ্রুপিং। তোমার পার্সারে `[` … `]` কে ১-এলিমেন্ট ম্যাট্রিক্স হিসেবে পার্স করো এবং
> পোস্টফিক্স `[n]` কে ইনডেক্স হিসেবে (১-ভিত্তিক)। **বা** সহজ পথ: পার্স করার আগে
> `][1]` → `)` এবং তারপর `[` → `(` রিপ্লেস করো — দুটোই সমান ফল দেয়। প্রথমটা বেশি নিরাপদ।

### 3.2 অপারেটর প্রিসিডেন্স (উঁচু → নিচু)

```
1. () এবং [..][..]           গ্রুপিং / ইনডেক্স
2. !                          পোস্টফিক্স ফ্যাক্টোরিয়াল
3. ^                          ঘাত — ডান-সহযোগী (right-associative)
4. unary -                    ঋণাত্মক  (⚠️ -2^2 = -(2^2) = -4)
5. * /  এবং ইমপ্লিসিট গুণ      বাম-সহযোগী, একই প্রিসিডেন্স
6. + -                        বাম-সহযোগী
```
> ইমপ্লিসিট গুণকে `*` এর সমান প্রিসিডেন্স ধরো, বাম-সহযোগী। Phase 9-এর
> `implicit multiplication test`, `order of basic operations test`, `percent test` — এই তিনটা
> পাস করলে ঠিক আছে। না করলে তখন প্রিসিডেন্স টিউন করবে (নোট নিয়ে রাখো)।

### 3.3 সংখ্যা মডেল

```kotlin
sealed interface MathValue {
    data class Real(val value: Double) : MathValue
    data class Cx(val re: Double, val im: Double) : MathValue
}
```
নিয়ম:
- সব অপারেশন `Real` দিয়েই করো যতক্ষণ না কমপ্লেক্স দরকার হয়।
- যেকোনো `Cx` যার `im == 0.0` — **অটো-ডিমোট করে `Real` বানাবে না**, শুধু
  `nthRootComplex` ফাংশনটাই `im == 0` হলে `Real` ফেরত দেয় (মূল কোডের মতো)।
- `Double` গণনা, কোনো `BigDecimal` নয় (math.js ডিফল্ট `number`)।

### 3.4 ফাংশন লাইব্রেরি (সবগুলো লাগবে)

| নাম | সংজ্ঞা |
|---|---|
| `sin, cos, tan` | রেডিয়ান |
| `asin, acos, atan` | রেডিয়ান |
| `sinh, cosh, tanh, asinh, acosh, atanh` | হাইপারবোলিক |
| `log(x)` | **প্রাকৃতিক লগ** (ln) — math.js-এর মতো |
| `log10(x)` | ভিত্তি-১০ লগ |
| `sind(x)` | `round(sin(PI/180 * x), 15)` |
| `cosd(x)` | `round(cos(PI/180 * x), 15)` |
| `tand(x)` | `round(tan(PI/180 * x), 15)` |
| `sing(x)` | `round(sin(PI/200 * x), 15)` |
| `cosg(x)` | `round(cos(PI/200 * x), 15)` |
| `tang(x)` | `round(tan(PI/200 * x), 15)` |
| `nthRootComplex(n, x)` | নিচে §3.4.1 |
| `derivate(eq, X, h=7e-4)` | নিচে §3.5 |
| `integrate(eq, a, b, n=50000, sig=10)` | নিচে §3.5 |
| `roundSignificant(v, places)` | `f = 10^ceil(log10(abs(v)))` ; `round(v/f, places) * f` |
| ফ্যাক্টোরিয়াল `!` | পূর্ণসংখ্যা n ≥ 0 → n!; নইলে গামা ফাংশন (math.js `gamma(n+1)`) |

`round(value, decimals)` = দশমিকের `decimals` ঘর পর্যন্ত, **টাই হলে শূন্য থেকে দূরে** (half away from zero)।

#### 3.4.1 `nthRootComplex(n, x)` — সাবধানে পড়ো

```
১. x-এর n-তম সব মূল (n সংখ্যক) বের করো।
২. এই তুলনাকারী দিয়ে সাজাও (ascending):
     a.im == b.im      → a.re - b.re
     a.im == 0         → +1   (বাস্তব মূল সবার শেষে যাবে)
     b.im == 0         → -1
     a.re == b.re      → a.im - b.im
     অন্যথায়            → a.re - b.re
৩. **শেষেরটা** (সবচেয়ে বড়) নাও।
৪. তার im == 0 হলে শুধু re (Real) ফেরত দাও, নইলে Cx ফেরত দাও।
```
**ফলাফল যা হওয়া উচিত:**
- `nthRootComplex(2, 9)` → `3.0` (ঋণাত্মক -3 নয়)
- `nthRootComplex(3, -27)` → `-3.0` (একমাত্র বাস্তব মূল)
- `nthRootComplex(2, -4)` → `Cx(0, 2)` (দুটো মূল ±2i, বড় im নেওয়া হয়)

**ফ্লোটিং-পয়েন্ট ফাঁদ:** পোলার (cos/sin) দিয়ে হিসাব করলে বাস্তব মূলেও ক্ষুদ্র `im` (≈1e-16) থেকে যায়,
তখন `im == 0` চেক ফেল করবে। তাই:
- `x` বাস্তব এবং (`n` বিজোড় **অথবা** `x >= 0`) হলে **সরাসরি** বাস্তব মূল হিসাব করো:
  `sign(x) * abs(x).pow(1.0/n)` (বিজোড় n) / `x.pow(1.0/n)` (জোড় n, x ≥ 0)।
- বাকি ক্ষেত্রে পোলার ব্যবহার করো এবং `abs(component) < 1e-12 * modulus` হলে সেটা `0.0` করে দাও।

### 3.5 `subres` মেকানিজম (∫ আর d/dx কীভাবে কাজ করে)

`∫` আর `d/dx` কে সাধারণ ফাংশন কল হিসেবে লেখা যায় না, কারণ ভেতরের রাশিটা **সিম্বলিক** (X সহ)।
তাই মূল কোড একটা টেক্সট-মার্কার ট্রিক ব্যবহার করে। এক্সপ্রেশন স্ট্রিং-এ এরকম থাকে:

```
(subres7idinsert)subres7idstart  <EQUATION>  subres7idparam <A>  subres7idparam <B>  subres7idend
```

**প্রি-প্রসেস অ্যালগরিদম** (`handle_subres`) — ইভ্যালুয়েশনের ঠিক আগে, লুপ করে যতক্ষণ `subres` শব্দ থাকে:
```
1. "subres" খুঁজে বের করো; তার পরের "id" পর্যন্ত অংশটাই আইডি N।
2. startIdx  = index of "subresNidstart"
   endIdx    = index of "subresNidend"
   insertIdx = index of "subresNidinsert"
3. params = substring(startIdx + len(startMarker) .. endIdx).split("subresNidparam")
4. value = subresFunctions[N](*params)          // params হলো **স্ট্রিং** (সাব-এক্সপ্রেশন)
5. insert মার্কারটার জায়গায় value বসাও
6. start..end পুরো অংশটা মুছে দাও
```
ফলাফল: `(<value>)` — আর কিছু থাকে না।

| এলিমেন্ট | `subresFunctions[N]` | params ক্রম |
|---|---|---|
| `d/dx` | `derivate` | `[equation, xValue]` |
| `∫` | `integrate` | `[equation, lowerLimit, upperLimit]` |

**`derivate(equation, X)`** — কেন্দ্রীয় অন্তরকলন:
```
h     = 7e-4
x     = evaluate(X)                 // X নিজেও একটা এক্সপ্রেশন স্ট্রিং
yMin  = evaluate(equation, X = x - h)
yMax  = evaluate(equation, X = x + h)
return (yMax - yMin) / (2 * h)
```

**`integrate(equation, a, b)`** — ট্র্যাপিজয়েড:
```
nSteps = 50_000        // ৫০,০০১ বার ইভ্যালুয়েট!
xMin = evaluate(a); xMax = evaluate(b)
h = (xMax - xMin) / nSteps
y[i] = evaluate(equation, X = xMin + i*h)   for i in 0..nSteps
sum  = Σ y  -  0.5*y[0]  -  0.5*y[nSteps]
return roundSignificant(sum * h, 10)
```

> ⚡ **পারফরম্যান্স (নেটিভে আবশ্যিক):** `equation`-টা **একবার** পার্স করে AST বানাও,
> তারপর ৫০,০০১ বার শুধু AST ইভ্যালুয়েট করো (প্রতিবার রি-পার্স করবে না)।
> তারপরও যদি ১৬ms-এর বেশি লাগে, `=` চাপার পর `Dispatchers.Default`-এ চালাও এবং
> UI-তে সংক্ষিপ্ত "busy" অবস্থা রাখো। **কখনোই মেইন থ্রেডে ANR করাবে না।**

### 3.6 Sexagesimal (`°`) রিরাইট রুল

`°` টোকেনটা পার্সার দেখে না — এক্সপ্রেশন স্ট্রিং তৈরির **শেষে** নিচের রেগেক্সগুলো **এই ক্রমে**
(global replace) চালাতে হবে। `D` = `[0-9.e]`:

| # | প্যাটার্ন | রিপ্লেসমেন্ট |
|---|---|---|
| 1 | `(D*°){4}` | `error` |
| 2 | `(D*°){3}D+` | `error` |
| 3 | `(D+)°(D+)°(D+)°` | `[$1+$2/60+$3/3600][1]` |
| 4 | `(D*°){3}` | `error` |
| 5 | `(D*°){2}D+` | `error` |
| 6 | `(D+)°(D+)°` | `[$1+$2/60][1]` |
| 7 | `(D*°){2}` | `error` |
| 8 | `(D*°){1}D+` | `error` |
| 9 | `(D+)°` | `[$1][1]` |
| 10 | `°` | `error` |

মানে: `1°30°` = 1.5 ; `1°30°36°` = 1.51 ; ভুল ফর্ম্যাট = `error` টোকেন → ইভ্যালুয়েশন ফেল।

### 3.7 এরর সেমান্টিক্স (হুবহু এই নিয়ম)

`=` চাপার পর:
```kotlin
val result = try { engine.evaluate(expr, scope) } catch (e: Throwable) { return EvalResult.Error }

val isBadValue = (result is Cx) || (result is Real && !result.value.isFinite())
val complexAllowed = (calcMode == CalcMode.CMPLX && result is Cx)
if (isBadValue && !complexAllowed) return EvalResult.Error
```
> **এরর কীভাবে দেখাবে:** ইনপুট-এরিয়ায় হুবহু `error` লেখা দেখাবে, আউটপুট-এরিয়া **খালি** থাকবে।
> আর `input_code_history` থেকে শেষের `key_equals` মুছে যাবে, যাতে ইউজার এডিট করা চালিয়ে যেতে পারে।
> (মূল কোডে এটাই হয় — Phase 5-এ আবার আসবে।)

- `NaN`, `±Infinity` → error
- COMP মোডে কমপ্লেক্স ফল → error
- CMPLX মোডে কমপ্লেক্স ফল → ঠিক আছে

### 3.8 ভেরিয়েবল স্কোপ

- ইভ্যালুয়েশনের সময় স্কোপে **শুধু `X`** থাকে: মান = `userVars["X"]` (না থাকলে `0.0`)।
- বাকি ভেরিয়েবল (`A..F`, `Y`, `M`, `Ans`) এক্সপ্রেশন স্ট্রিং বানানোর সময়ই **তাদের মান দিয়ে রিপ্লেস**
  হয়ে যায় (`(3.5)` এর মতো)।
- ⚠️ **স্ট্রিং-এ মান বসানোর সময় প্রিসিশন হারিও না।** সবচেয়ে ভালো: `VarNode` একটা প্লেসহোল্ডার
  টোকেন (যেমন `@v3`) দিক আর পাশে একটা `Map<String, MathValue>` রাখো; পার্সার `@v3` দেখলে
  সরাসরি ওই `MathValue` বসাবে। এতে `Double.toString()` রাউন্ড-ট্রিপের ঝামেলা থাকে না এবং
  কমপ্লেক্স `Ans`-ও ঠিকঠাক কাজ করে।

### টাস্ক

- [ ] **3.1** `Complex.kt` — add/sub/mul/div/pow/abs/arg/exp/log/sqrt/nthRoots
- [ ] **3.2** `Tokenizer.kt` — সংখ্যা (`e` এক্সপোনেন্টসহ), আইডেন্টিফায়ার, অপারেটর, `[`/`]`, `(`/`)`, `,`, `!`, `@`-প্লেসহোল্ডার
- [ ] **3.3** `Ast.kt` + `Parser.kt` — recursive descent, §3.2-এর প্রিসিডেন্স, ইমপ্লিসিট গুণ
- [ ] **3.4** `MathFunctions.kt` — §3.4-এর সব ফাংশন
- [ ] **3.5** `Evaluator.kt` — AST → `MathValue`, স্কোপ সাপোর্ট
- [ ] **3.6** `SubresProcessor.kt` — §3.5-এর প্রি-প্রসেস + derivate/integrate
- [ ] **3.7** `SexagesimalRewriter.kt` — §3.6-এর ১০টা রেগেক্স, ঠিক ক্রমে
- [ ] **3.8** `MathEngine.kt` — ফ্যাসাড: `evaluate(expr: String, values: Map<String,MathValue>, x: Double): MathValue`

### DoD — এই ইউনিট টেস্টগুলো পাস করতেই হবে

```kotlin
"12+4*5-8/3+7"                            → 36.33333333…
"12(3.141592653589793)+5(3-7)-8log10(4)"  → 12.88263191…
"([150238][1]^[2][1])"                    → 2.257145664e10
"nthRootComplex(3,(0-27))"                → -3.0
"nthRootComplex(2,(0-4))"                 → Cx(0, 2)
"8!"                                      → 40320
"([1][1]/[2][1])"                         → 0.5
"(1/log([3][1])*log([85][1]))"             → log base 3 of 85
"5e3"                                     → 5000.0
"1°30°"  (রিরাইট করার পর)                  → 1.5
"sind(30)"                                → 0.5  (হুবহু, 0.49999999999 নয়)
"cosd(90)"                                → 0.0  (হুবহু)
integrate("X^2", "0", "1")                → 0.3333333333 (≈1/3)
derivate("X^3", "2")                      → ≈12.0
```

---

# Phase 4 — Input Model (গণিত-এলিমেন্ট ট্রি)

এটাই অ্যাপের হৃদয়। ভুল করলে ডিসপ্লে আর কার্সর দুটোই ভাঙবে।

### 4.1 মূল ধারণা

প্রতিটা এলিমেন্টের **৪টা প্রতিবেশী** আছে — একটা 2D গ্রাফ:

```
index 0 = left      index 1 = down      index 2 = right     index 3 = up
```
- `right`/`left` = লিনিয়ার লেখার ক্রম (ডিসপ্লে এই ক্রমেই রেন্ডার হয়)
- `up`/`down` = ভগ্নাংশের লব↔হর, ঘাতের ভিত্তি↔সূচক ইত্যাদিতে লাফ দেওয়ার জন্য

```kotlin
class MathNode(
    val kind: NodeKind,
    val display: DisplayFragment,   // কীভাবে দেখাবে
    val expr: String,               // এক্সপ্রেশনে কী যোগ হবে
) {
    var left: MathNode? = null
    var down: MathNode? = null
    var right: MathNode? = null
    var up: MathNode? = null
    var children: List<MathNode>? = null   // শুধু ContainerOp-এর
    var parent: MathNode? = null           // শুধু Container-এর
    var isLastContainer = false
    var skipToAfterCreation: MathNode? = null
}
```

### 4.2 `NodeKind` (মূল কোডের `type` স্ট্রিং)

| kind | কারা | নোট |
|---|---|---|
| `Start` | লিস্টের শুরুর সেন্টিনেল | `expr = ""` |
| `Int` | `0`–`9` | প্রতিটা ডিজিট **আলাদা** নোড |
| `AdditiveOp` | `+` `-` | `get_left_block` এখানে থামে |
| `MultiOp` | `×` `÷` | `get_left_block` এখানে থামে |
| `PointOp` | `.` `×⒑` `!` `°` | |
| `Sto` | `→A` `M+` `M-` | সবসময় সবার শেষে বসে |
| `BracketsOp` | `(` এবং সব ফাংশন-ওপেন (`sin(`, `log(`, …) | ব্র্যাকেট কাউন্টার −1 |
| `BracketsClose` | `)` | ব্র্যাকেট কাউন্টার +1 |
| `Var` | `Ans`, `A`–`F`, `X`, `Y`, `M`, `π`, `e`, `%`, ৪০টা ধ্রুবক, `i` | `expr` রানটাইমে মান থেকে তৈরি |
| `ContainerOp` | ভগ্নাংশ, √, ⁿ√, ঘাত, log_n, d/dx, ∫ | `children` থাকে |
| `Container` | ContainerOp-এর স্লট-বিভাজক | `parent` + `isLastContainer` |

### 4.3 `isFamily(a, b)` — খালি স্লট চেনার নিয়ম

```
যদি a বা b কোনোটা null → false
যদি a.kind == ContainerOp → parent = a, child = b
নইলে যদি b.kind == ContainerOp → parent = b, child = a
নইলে:
    যদি দুটোই Container → return a.parent === b.parent
    নইলে → false
যদি child.kind == Container → return parent === child.parent
নইলে → false
```
**ব্যবহার:** রেন্ডার করার সময় যদি `isFamily(node, node.right)` সত্য হয়, মানে ওই স্লটটা **খালি** →
সেখানে `▯` (U+25AF) প্লেসহোল্ডার আঁকবে।

### 4.4 `getLeftBlock(left)` — ভগ্নাংশ/ঘাত কতটুকু "গিলে ফেলবে"

`key_frac` বা `key_pown` চাপলে বাঁ দিকের কতটুকু অংশ লব/ভিত্তি হবে, সেটা এই ফাংশন ঠিক করে।
রিটার্ন করে তিনটা: `(stopNode, lastElement, firstElement)`

```
node = left
bracketCounter = 0
if node is BracketsOp    → bracketCounter -= 1
if node is BracketsClose → bracketCounter += 1

while node.kind != Start
      && bracketCounter >= 0
      && ( node.kind !in [AdditiveOp, MultiOp, Sto] || bracketCounter > 0 ):

    if firstElement == null: firstElement = node

    if node.kind == Container:
        if node.isLastContainer: node = node.parent!!     // পুরো কনস্ট্রাক্টটা গিলে ফেলো
        else: break                                       // মাঝের স্লট পেরোনো যাবে না
    else if node.kind == ContainerOp: break

    lastElement = node
    node = node.left!!

    if node is BracketsOp    → bracketCounter -= 1
    if node is BracketsClose → bracketCounter += 1

return (node, lastElement, firstElement)
```
অর্থাৎ: `+`, `−`, `×`, `÷` বা STO-তে এসে থেমে যায় — **কিন্তু** যদি তখন আমরা একজোড়া বন্ধনীর
ভেতরে থাকি (`bracketCounter > 0`) তাহলে থামে না। উদাহরণ: `5+3^` → শুধু `3` ভিত্তি হবে;
`(5+3)^` → পুরো `(5+3)` ভিত্তি হবে।

### 4.5 কনটেইনার এলিমেন্টগুলোর গঠন

প্রতিটা `ContainerOp` = একটা "ওপেনিং" নোড + কিছু `Container` স্লট-বিভাজক।
ডিসপ্লে ও এক্সপ্রেশন টুকরোগুলো পরপর জোড়া লেগে পুরোটা বানায়।

| ContainerOp | ওপেনিং expr | children[i] expr | শেষ কনটেইনার? |
|---|---|---|---|
| **Frac** (`key_frac`) | `([` | `[0]: ][1]/[` , `[1]: ][1])` | `[1]` |
| **Sqrt** (`key_sqrt`) | `nthRootComplex(2,[` | `[0]: ][1])` | `[0]` |
| **Sqrtⁿ** (`key_sqrtn`) | `nthRootComplex([` | `[0]: ][1],[` , `[1]: ][1])` | `[1]` |
| **Pow** (`key_pown`) | `([` | `[0]: ][1]^[` , `[1]: ][1])` | `[1]` |
| **Logₙ** (`key_logn`) | `(1/log([` | `[0]: ][1])*log([` , `[1]: ][1]))` | `[1]` |
| **d/dx** (`key_deriv`) | `(subresNidinsert)subresNidstart` | `[0]: subresNidparam` , `[1]: subresNidend` | `[1]` |
| **∫** (`key_integ`) | `(subresNidinsert)subresNidstart` | `[0]: subresNidparam` , `[1]: subresNidparam` , `[2]: subresNidend` | `[2]` |

**Frac ও Pow-এর বিশেষ আচরণ** (বাঁয়ের ব্লক গিলে ফেলা):
```
(stop, last, first) = getLeftBlock(left)
নতুন নোডটা বসবে stop-এর ডানে
যদি last != null:
    last.left = নতুন নোড
    children[0].left = first          // ব্লকটা এখন ওপেনিং আর children[0]-এর মাঝখানে
    // Frac হলে অতিরিক্ত: ব্লকের প্রতিটা এলিমেন্টের down = children[0]
    //                   (যদি তার down আগে থেকে ব্লকের ভেতরেই না থাকে)
    skipToAfterCreation = Frac ? children[0] : (isExpPrefilled ? children[1] : children[0])
নইলে:
    children[0].left = নতুন নোড
```
Frac-এ আরও দুটো লাইন (হুবহু রাখবে, এগুলোই up/down নেভিগেশন চালায়):
```
fracNode.down            = children[0]
children[0].up           = children[0]      // হ্যাঁ, নিজেই নিজের up
```
> `up` চাপলে কোড করে: `cursor = cursor.up; তারপর cursor = cursor.left` — তাই হর থেকে `up`
> চাপলে কার্সর লবের শেষে চলে যায়। এটা ইচ্ছাকৃত।

### 4.6 নতুন এলিমেন্ট ঢোকানোর ওয়্যারিং (প্রতিটা কী-প্রেসে)

```
oldNeighbors = [cursor.left, cursor.down, cursor.right, cursor.up]   // তৈরির আগে স্ন্যাপশট
newElements  = [] ; (switch অনুযায়ী নোড তৈরি ও cursor সরানো)

if newElements.isNotEmpty():
    newElements[0].skipToAfterCreation?.let { cursor = it }

    if oldNeighbors.right != null:
        if newElements[0].children != null:
            val lastChild = newElements[0].children!!.last()
            lastChild.right = oldNeighbors.right ; oldNeighbors.right.left = lastChild
        else:
            newElements[0].right = oldNeighbors.right ; oldNeighbors.right.left = newElements[0]

    for (n in newElements):
        if n.kind == ContainerOp:
            setContainerNeighbors(n, oldNeighbors)
        else:
            if n.down == null: n.down = oldNeighbors.down
            if n.up   == null: n.up   = oldNeighbors.up
```

`setContainerNeighbors(node, oldNeighbors)`:
```
for dir in [down, up]:
    val nb = oldNeighbors[dir] ?: continue
    var e: MathNode? = node
    while (e != node.children!!.last().right) {
        if (e[dir] == null) e[dir] = nb
        e = e!!.right
    }
```

> ⚠️ মূল কোডে `Container_Element.set_neighbor()`-এ একটা **অসীম লুপ বাগ** আছে
> (`while(!is_family(...))` লুপে next_element কখনো বাড়ে না)। ওই কোডপাথ বাস্তবে কখনো চলে না।
> **এটা পোর্ট করবে না।**

### 4.7 কার্সর মুভমেন্ট

| কী | আচরণ |
|---|---|
| `key_dir0` (◀ বাম) | `cursor.left` থাকলে সেখানে যাও; **না থাকলে** ডান দিকে একদম শেষ পর্যন্ত গিয়ে থামো (র‍্যাপ-অ্যারাউন্ড) |
| `key_dir2` (▶ ডান) | `cursor.right` থাকলে সেখানে; না থাকলে বাম দিকে একদম শুরুতে |
| `key_dir3` (▲ উপর) | `cursor.up` থাকলে `cursor = cursor.up`, **তারপর আরও এক ধাপ** `cursor = cursor.left` |
| `key_dir1` (▼ নিচ) | `cursor.down` থাকলে `cursor = cursor.down` |
| `pos1` (ভার্চুয়াল) | একদম বাঁ প্রান্তে |
| `end` (ভার্চুয়াল) | একদম ডান প্রান্তে |

**বিশেষ:** `key_dir1`/`key_dir3` চাপার সময় যদি সমীকরণটা **সম্পূর্ণ খালি** হয়
(`cursor.kind == Start && cursor.right == null`), তাহলে কার্সর নড়ে না — বরং:
```
এই খালি সমীকরণটা লিস্ট থেকে মুছে ফেলো
active handler = EquationListHandler (প্যারেন্ট)
displayIndex = (dir1 ? 0 : equations.lastIndex)
```
মানে: খালি অবস্থায় ▲/▼ চাপলে আগের হিসাবগুলোর ইতিহাসে (replay) ফিরে যায়।

### 4.8 ডিলিট (`key_del`)

```
when (cursor.kind) {
  Start       -> কিছুই না
  Container   -> cursor = cursor.left        // মুছবে না, শুধু স্লটের বাইরে বেরোবে
  ContainerOp -> {
      val toDelete = listOf(cursor) + cursor.children!!
      cursor = cursor.left!!
      var e: MathNode? = toDelete.last()
      while (e != toDelete.first().left) {
          if (e in toDelete) {
              e.left!!.right = e.right
              e.left!!.right?.left = e.left
          } else {
              if (e.down in toDelete) e.down = toDelete.last().down
              if (e.up   in toDelete) e.up   = toDelete.last().up
          }
          e = e!!.left
      }
      // ফলাফল: র‍্যাপারটা মুছে গেল, **ভেতরের লেখা থেকে গেল**
  }
  else -> {
      cursor = cursor.left!!
      cursor.right = oldNeighbors.right
      cursor.right?.left = cursor
  }
}
```

### 4.9 রেন্ডার + এক্সপ্রেশন একসাথে তৈরি করা (একটাই পাস)

```
res = "" ; expr = "" ; sto = null ; placeholderSelected = false
var node: MathNode? = startNode
while (node != null) {
    // ---- ডিসপ্লে অংশ ----
    if ((node.kind == ContainerOp || node.kind == Container) && isFamily(node, node.right)) {
        res += node.display
        if (showCursor && node == cursorNode) { res += CURSOR ; placeholderSelected = true }
        res += "▯"
    } else if (node.kind != Start) {
        res += node.display
    }
    if (showCursor && node == cursorNode && !placeholderSelected) res += CURSOR

    // ---- এক্সপ্রেশন অংশ ----
    if (sto != null) { expr = "error" ; break }
    when (node.kind) {
        Var -> expr += "(" + node.currentValue() + ")"
        Sto -> sto = node
        else -> expr += node.expr
    }
    node = node.right
}
res += " "            // শেষে সবসময় একটা NBSP
expr = rewriteSexagesimal(expr)   // §3.6
```
> `Start` নোডের ডিসপ্লে খালি, কিন্তু কার্সর ওখানে থাকলে কার্সর আঁকতে হবে।

### টাস্ক
- [ ] **4.1** `NodeKind`, `MathNode`, `DisplayFragment` ডেটা মডেল
- [ ] **4.2** `isFamily`, `getLeftBlock`, `setContainerNeighbors` হেল্পার
- [ ] **4.3** `ElementFactory` — Appendix C-র পুরো টেবিল অনুযায়ী প্রতিটা `KeyCode` → নোড
- [ ] **4.4** `TreeBuilder.build(history: List<KeyCode>): BuildResult` (ট্রি + কার্সর + calcOutput ফ্ল্যাগ)
- [ ] **4.5** কার্সর মুভমেন্ট + ডিলিট
- [ ] **4.6** `render(tree, cursor, showCursor): Pair<DisplayNode, String>`

### DoD
- [ ] ইউনিট টেস্ট: `[frac, 1, 9, dir2, 2, 1, dir2, div, frac, 2, dir2, 7]` → expr
      `([19][1]/[21][1])/([2][1]/[7][1])` এবং ডিসপ্লে ট্রি-তে দুটো Frac নোড
- [ ] `[1,2,shift,pow10,plus,5,lparen,3,minus,7,rparen,minus,8,log,4,rparen]`
      → `12(3.141592653589793)+5(3-7)-8log10(4)`
- [ ] `[5,plus,3,pown,2]` → ভিত্তি শুধু `3`; `[lparen,5,plus,3,rparen,pown,2]` → ভিত্তি পুরো `(5+3)`
- [ ] খালি Frac-এ `del` চাপলে সব মুছে যায়

---

# Phase 5 — Input Handler স্টেট মেশিন

### 5.1 হ্যান্ডলার হায়ারার্কি

| হ্যান্ডলার | কাজ |
|---|---|
| `EquationListHandler` | **রুট।** সব সমীকরণের লিস্ট, `Ans`, ইউজার ভেরিয়েবল, ফরম্যাট টগল, রিপ্লে |
| `EquationInputHandler` | একটা সমীকরণ টাইপ করার অবস্থা (`inputCodeHistory` রাখে) |
| `ComplexEquationHandler` | উপরেরটার সাবক্লাস — শুধু `cmplx` মোড-ম্যাপ অতিরিক্ত প্রয়োগ করে |
| `ConstSelectHandler` | `CONST` মেনু — ২ ডিজিট (01–40) |
| `HypSelectHandler` | `hyp` মেনু — ১ ডিজিট (1–6) |
| `ModeSelectHandler` | `MODE` মেনু — 1:COMP / 2:CMPLX |
| `SetupSelectHandler` | `SETUP` মেনু — 3:Deg 4:Rad 5:Gra 6:Fix 8:Norm (+ সাব-মেনু) |
| `TurnedOffHandler` | "OFF" দেখায়; `key_on` চাপলে অ্যাপ রিস্টার্ট |
| `VoidHandler` | অ্যাপ বন্ধ (শুধু টেস্টে দরকার) |

একটাই "active handler" থাকে (`CalculatorViewModel`-এ `activeHandler` স্টেট)।

### 5.2 মোড টগল (shift / alpha / STO)

তিনটা মোড, **একসাথে একটার বেশি নয়**:
```
fun toggleMode(target: Mode?) {
    modes.keys.forEach { modes[it] = (it == target) && !modes[it]!! }
    // অর্থাৎ: target নিজে হলে উল্টে যায়, বাকিগুলো সবসময় false
    ui.updateIndicators(modes)
}
```
কী চাপলে:
- `key_shift` → toggle "shift"
- `key_alpha` → toggle "alpha"
- অন্য যেকোনো কী → চালু মোডের ম্যাপ থেকে রিম্যাপ করো, **তারপর সব মোড অফ করো**
- shift ম্যাপে `key_rcl → key_STO` পেলে: এলিমেন্ট বানাবে না, বরং **"STO" মোড অন** করবে
- ম্যাপে কী না পেলে → ওই কী-প্রেস **বাতিল** (কিছুই হয় না), কিন্তু মোড অফ হয়ে যায়

পুরো ম্যাপ → **Appendix B**।

### 5.3 `EquationInputHandler.handle(code)` ধাপে ধাপে

```
1. shift/alpha হলে শুধু মোড টগল করে শেষ
2. মোড ম্যাপিং প্রয়োগ → inputCodeHistory-তে push
3. যদি push করা কোডটা STO/M+/M− হয়:
       history-তে শেষের ঠিক আগে "end" ঢোকাও  এবং  শেষে "key_equals" push করো
       (মানে: কার্সর শেষে নাও → STO বসাও → সাথে সাথে হিসাব করো)
4. শেষ কোড অনুযায়ী বিশেষ কাজ:
       key_ac    → history সম্পূর্ণ খালি, সব মোড অফ
       key_CONST → ConstSelectHandler চালু, history থেকে শেষ কোডটা সরাও
       key_hyp   → HypSelectHandler চালু, শেষ কোড সরাও
       key_mode  → ModeSelectHandler চালু, শেষ কোড সরাও
       key_setup → SetupSelectHandler চালু, শেষ কোড সরাও
       key_off   → turnOffClose সেটিং অন হলে অ্যাপ বন্ধ, নইলে TurnedOffHandler
5. active handler-এর updateDisplay(showCursor = true) + updatePosition()
```
> ⚠️ মূল JS-এ `pop(2)` লেখা আছে — কিন্তু JS-এর `Array.pop()` আর্গুমেন্ট উপেক্ষা করে,
> মানে **একটাই** এলিমেন্ট সরে। Kotlin-এ `removeLast()` একবার। (এটা ভুল করা খুব সহজ।)

### 5.4 `=` চাপলে কী হয়

```
1. showCursor=false দিয়ে ট্রি রেন্ডার করে inputString + expr বের করো
2. expr-এ handleSubres() চালাও (§3.5)
3. X = userVars["X"] ?: 0.0
4. result = evaluate(expr, X)      // এরর/Infinity/COMP-এ complex → "error"
5. যদি error:
       history থেকে শেষের key_equals সরাও
       ইনপুট-এরিয়ায় "error", আউটপুট খালি — **হ্যান্ডলার বদলাবে না**
   নইলে:
       sto থাকলে sto.operate(result) চালাও (ভেরিয়েবলে সেভ / M+ / M−)
       active handler = EquationListHandler
       parent.inputStrings[displayIndex] = inputString
       parent.results[displayIndex]      = result
       parent.updateDisplay(showCursor = false)
```

`sto.operate(value)`:
- `→A`…`→F`, `→X`, `→Y`, `→M` : `userVars[name] = value`
- `M+` : `userVars["M"] += value`
- `M−` : `userVars["M"] -= value`

### 5.5 `EquationListHandler` (রিপ্লে / ইতিহাস)

স্টেট: `equations: MutableList<EquationInputHandler>`, `inputStrings`, `results`,
`displayIndex`, `userVars (শুরুতে {"M" to 0.0})`, `formatAs`।

```
key_equals  → নতুন সমীকরণ যোগ করো, তাতে **বর্তমানটার পুরো history কপি** করো,
              displayIndex = শেষ, active = ওই নতুনটা → (এডিট করা যায়)
key_dir0 (◀)→ নতুন সমীকরণ, history = বর্তমানের history.dropLast(1)  (শেষের key_equals বাদ),
              active = নতুনটা, তারপর "end" পাঠাও  → কার্সর ডান প্রান্তে
key_dir2 (▶)→ একই, কিন্তু "pos1" পাঠাও → কার্সর বাম প্রান্তে
key_dir1 (▼)→ displayIndex + 1 (ক্ল্যাম্প করা), formatAs রিসেট
key_dir3 (▲)→ displayIndex − 1 (ক্ল্যাম্প করা), formatAs রিসেট
key_SD      → formatAs: fraction ⇄ decimal
key_deg     → formatAs: sexagesimal ⇄ decimal
key_eng     → formatAs eng হলে level +1 (সর্বোচ্চ 9), নইলে "eng4"
key_back    → (shift+eng) eng হলে level −1 (সর্বনিম্ন 1), নইলে "eng4"
key_shift / key_alpha / key_STO → মোড টগল
অন্য সব    → নতুন খালি সমীকরণ বানাও, active করো, তারপর:
              যদি কী টা  + − × ÷  বা  key_STO_*  হয় → আগে "key_Ans" পাঠাও
              তারপর আসল কী টা পাঠাও
```
`formatAs` রিসেট মানে: `if (preferDecimals) "decimal" else "fraction"`।

> ⚠️ মূল কোডে `max_equations = 15` আর `ans_value = 0` ফিল্ড দুটো **কখনো ব্যবহারই হয় না**।
> তাই ১৫টার কোনো লিমিট **বসাবে না** — তাহলে আচরণ বদলে যাবে।

### 5.6 `Ans`

`Ans` নোডের মান = `results.last()` — অর্থাৎ **সবচেয়ে শেষে হিসাব করা ফল**,
বর্তমানে যেটা স্ক্রিনে দেখাচ্ছে সেটা নয় (রিপ্লে করে পুরোনো সমীকরণে গেলে পার্থক্য বোঝা যাবে)।
`results` খালি থাকলে `Ans` = `undefined` → এক্সপ্রেশনে `(undefined)` → এরর। এই আচরণটাই রাখো
(Kotlin-এ: মান না থাকলে এমন টোকেন দাও যেটা পার্স ফেল করবে)।

### 5.7 মেনু হ্যান্ডলারগুলো

**`ConstSelectHandler`** — `maxInput = [4, 0]` (সর্বোচ্চ "40")
- ডিসপ্লে: ইনপুট-এরিয়ায় `KONSTANTE` / `Nummer 01~40?`, আউটপুট-এরিয়ায় `[_ _]` → `[0 5]`
- ডিজিট-বাই-ডিজিট যাচাই: প্রতিটা ধাপে এখন পর্যন্ত টাইপ করা সংখ্যা `maxInput`-এর চেয়ে বড় হলে
  ওই ডিজিট বাতিল। `00` ও বাতিল।
- ২ ডিজিট পূর্ণ হলে → parent-এ `key_CONST_<NN>` পাঠাও
- `key_ac` → বাতিল করে ফিরে যাও ; `key_on` → অ্যাপ রিলোড

**`HypSelectHandler`** — `maxInput = [6]`
- ডিসপ্লে (ইনপুট-এরিয়ায়, উপরে-বাঁয়ে, ছোট ফন্টে):
  ```
  1:sinh   2:cosh
  3:tanh   4:sinh-1
  5:cosh-1 6:tanh-1
  ```
- ম্যাপ: `1→key_sinh, 2→key_cosh, 3→key_tanh, 4→key_asinh, 5→key_acosh, 6→key_atanh`

**`ModeSelectHandler`**
- ডিসপ্লে: `1:COMP   2:CMPLX`
- `key_1 → COMP`, `key_2 → CMPLX` → সেটিং সেভ করে **অ্যাপ রিস্টার্ট** (নিচে দেখো)
- `key_ac` / `key_mode` → বাতিল ; `key_on` → রিলোড

**`SetupSelectHandler`**
- ডিসপ্লে (কিছু স্লট ইচ্ছে করেই খালি):
  ```
  1:______2:______
  3:Deg   4:Rad
  5:Gra   6:Fix
  7:______8:Norm
  ```
- `3→Deg, 4→Rad, 5→Gra` → সেভ + রিস্টার্ট
- `6→Fix` → সাব-মেনু `Fix 0~9?` → `key_0..key_9` → `Fix_0..Fix_9` → সেভ + রিস্টার্ট
- `8→Norm` → সাব-মেনু `Norm 1~2?` → `key_1/key_2` → `Norm_1/Norm_2` → সেভ + রিস্টার্ট
- `key_ac` / `key_mode` → বাতিল

**`TurnedOffHandler`**
- ইনপুট-এরিয়া: `OFF`, আউটপুট-এরিয়া খালি
- যেকোনো কী উপেক্ষা, শুধু `key_on` → অ্যাপ রিস্টার্ট

### 5.8 "অ্যাপ রিলোড / বন্ধ" — নেটিভে কীভাবে

ওয়েব ভার্সন `location.reload()` আর `window.close()` ব্যবহার করে। নেটিভে:

| ওয়েব | Android |
|---|---|
| `location.reload()` | ViewModel-এর পুরো স্টেট নতুন করে বানাও (`resetAll()`) — **Activity রিক্রিয়েট করবে না** |
| `window.close()` | `(context as Activity).finishAndRemoveTask()` |
| বন্ধ করা না গেলে অ্যালার্ট | AlertDialog: *"Can not close app! … Disable closing?"* → OK চাপলে `turnOffClose = false` সেভ করে reset |

`key_off` এর নিয়ম: সেটিংসে **"Turn Off = Close App"** চালু থাকলে অ্যাপ বন্ধ, নইলে "OFF" স্ক্রিন।

### 5.9 সেটিংস পারসিস্টেন্স (localStorage → DataStore)

| localStorage কী | DataStore কী | টাইপ | ডিফল্ট |
|---|---|---|---|
| `decimalFormat` | `decimal_format` | `"."` / `","` | ডিভাইস লোকেল থেকে |
| `preferDecimal` | `prefer_decimal` | Boolean | `false` |
| `turnOffClose` | `turn_off_close` | Boolean | `false` |
| `calcMode` | `calc_mode` | `COMP` / `CMPLX` | `COMP` |
| `roundingMode` | `rounding_mode` | `Norm_1`,`Norm_2`,`Fix_0`..`Fix_9` | `Norm_1` |
| `angleMode` | `angle_mode` | `Deg`/`Rad`/`Gra` | `Deg` |
| `lastSeenVersionCode` | `last_seen_version` | Int | `0` |
| `selectedDesign` | — | **বাদ** (একটাই ডিজাইন) | — |

`decimalFormat == "."` হলে লোকেল `en-US`, নইলে `de-DE` — এটা সংখ্যা ফরম্যাটিং আর
কমার লেবেল দুটোকেই নিয়ন্ত্রণ করে।

### 5.10 হার্ডওয়্যার কীবোর্ড সাপোর্ট (থাকলে ভালো, মূল অ্যাপে আছে)

`Modifier.onKeyEvent` দিয়ে:
`0-9`→ডিজিট, `+ - * / ( )`, `Enter`→`=`, `Backspace/Delete`→`DEL`,
`Space/Tab`→`AC`, `x/X`→ভেরিয়েবল X, `. ,`→কমা,
তীর চারটা→`dir0..dir3`, `Home`→`MODE`,
`F1`→SHIFT, `F2`→ALPHA, `F3`→MODE, `F4`→ON।

### DoD
- [ ] shift→`x⁻¹` চাপলে `x!` বসে; alpha→`(−)` চাপলে ভেরিয়েবল `A` বসে
- [ ] `5 = ` তারপর `+ 3 =` → `Ans+3` = 8
- [ ] `9 shift+RCL (−)` → `9→A` এবং `A` এর মান 9
- [ ] `MODE 2` → CMPLX ইন্ডিকেটর জ্বলে, রিস্টার্টের পরেও থাকে
- [ ] `SHIFT MODE 6 3` → Fix_3 সেট, ইন্ডিকেটর `FIX` জ্বলে
- [ ] `SHIFT AC` → "OFF" দেখায়, `ON` চাপলে ফিরে আসে

---

# Phase 6 — Number Formatting

`NumberFormatter.format(value: MathValue, formatAs: FormatMode, roundingMode, locale): DisplayNode`

`FormatMode` = `Fraction` | `Decimal` | `Sexagesimal` | `Eng(level: Int /*1..9*/)`

### 6.1 অ্যালগরিদম (হুবহু এই ক্রমে)

```
0. মান যদি "error"/খালি হয় → যা আছে তাই ফেরত দাও

1. কমপ্লেক্স হলে:
      reStr = format(re, formatAs, epsilon = 1e-14)
      imStr = format(im, formatAs, epsilon = 1e-14)
      out = ""
      if (reStr != "0") out += reStr
      if (imStr != "0") {
          if (reStr != "0") {
              if (formatAs != Fraction) out += লাইনব্রেক
              out += "+"
          }
          if (imStr != "1") out += imStr
          out += "i"
      }
      return if (out.isEmpty()) "0" else out

2. Fraction মোড:
      (num, den) = continuedFractionOf(value, epsilon = 4.5e-16)
      if (den != 1 && (num.toString().length + den.toString().length) <= 9)
          → ভগ্নাংশ হিসেবে দেখাও (লব/হর, মাঝে দাগ)
      নইলে নিচের ৫ নম্বরে যাও (দশমিক)

3. Sexagesimal মোড:
      d = floor(v) ;  m = floor((v - d) * 60)
      s = round((v - d - m/60) * 3600 * 100) / 100
      → "d°m′s‴"                      (উদাহরণ: 1°7′24.44‴)

4. Eng(level) মোড:
      exponent  = (floor(log10(v) / 3) + level - 4) * 3
      factor    = 10^exponent
      n         = v / factor
      decimals  = if (roundingMode is Fix_k) k else 9
      n         = round(n, decimals)
      → localeNumber(n, maxSignificantDigits = 10) + "×⒑" + superscript(exponent)

5. দশমিক (ডিফল্ট পথ):
      if (roundingMode is Fix_k) v = round(v, k)
      lowerSciBorder = if (roundingMode == Norm_2) 1e-9 else 1e-2
      if (abs(v) >= 1e10 || (v != 0.0 && abs(v) < lowerSciBorder)) {
          (coeff, exp) = v.toExponential(9)        // ৯ দশমিক = ১০ সার্থক অঙ্ক
          exp থেকে '+' চিহ্ন সরাও
          → localeNumber(coeff, maxSignificantDigits = 10) + "×⒑" + superscript(exp)
      }
      → localeNumber(v, maxSignificantDigits = 10)
```

### 6.2 `localeNumber(v, maxSignificantDigits = 10)`

JS-এর `Intl.NumberFormat(lang, { useGrouping: false, maximumSignificantDigits: 10 })` এর সমতুল্য:
- হাজারের কোনো সেপারেটর **নেই**
- সর্বোচ্চ ১০ সার্থক অঙ্ক, রাউন্ডিং = **half away from zero**
- পেছনের অপ্রয়োজনীয় শূন্য কেটে দাও (`0.500` → `0.5`)
- দশমিক চিহ্ন: `en-US` → `.` , `de-DE` → `,`

Kotlin-এ:
```kotlin
val bd = BigDecimal(v).round(MathContext(10, RoundingMode.HALF_UP)).stripTrailingZeros()
val text = bd.toPlainString().replace('.', decimalSeparator)
```
> `HALF_UP` in `BigDecimal` = half away from zero — ঠিক আছে।
> `toPlainString()` ব্যবহার করো, `toString()` নয় (নইলে E-নোটেশন চলে আসবে)।

### 6.3 Continued Fraction

```kotlin
fun parseContinuedFraction(cf: List<Long>): Pair<Long, Long> {
    var (n, d) = 1L to 0L
    for (i in cf.indices.reversed()) { val nn = cf[i] * n + d; d = n; n = nn }
    return n to d                       // (লব, হর)
}

fun continuedFractionOf(x: Double, epsilon: Double): Pair<Long, Long> {
    var cur = x
    var k = floor(cur).toLong()
    val cf = mutableListOf(k)
    var rem = cur - k
    var frac = parseContinuedFraction(cf)
    var guard = 0
    while (abs(frac.first.toDouble() / frac.second - x) > epsilon) {
        if (++guard > 10_000) break            // ANR-প্রতিরোধক; বাস্তবে কখনো লাগে না
        cur = 1.0 / rem
        k = floor(cur).toLong()
        cf.add(k)
        rem = cur - k
        frac = parseContinuedFraction(cf)
    }
    return frac
}
```
- epsilon ডিফল্ট `4.5e-16`; কমপ্লেক্সের re/im-এর জন্য `1e-14`।
- হর `1` হলে ভগ্নাংশ দেখাবে না।
- `লব.লেন + হর.লেন > 9` হলেও ভগ্নাংশ দেখাবে না।

### 6.4 টেস্ট (Phase 9-এর সাথে মিলবে)

| ইনপুট | মোড | আউটপুট |
|---|---|---|
| `0.5` | Fraction | `1/2` |
| `19.0/6` | Fraction | `19/6` |
| `π` | Decimal, en-US | `3.141592654` |
| `1.1` | Decimal, de-DE | `1,1` |
| `0.005` | Decimal, Norm_1 | `5×⒑⁻³` |
| `0.005` | Decimal, Norm_2 | `0.005` |
| `1.123456789` | Fix_3 | `1.123` |
| `1.123456789` | Fix_0 | `1` |
| `22346368.72e-9` | Eng(…) | `22346368.72×⒑⁻⁹` |
| `1.1233…` | Sexagesimal | `1°7′24.44‴` |
| `Cx(3, 0.25)` | Decimal | `3` ⏎ `+0.25i` |
| `Cx(0, 1)` | যেকোনো | `i` |

---

# Phase 7 — Math Display Renderer (Compose টাইপসেটিং)

ওয়েব ভার্সন nested `<span>` + CSS দিয়ে গণিত সাজায়। আমরা Compose-এ নিজেদের
**measure → draw** ইঞ্জিন লিখব। এটা করার সবচেয়ে নিয়ন্ত্রিত উপায়:
`Canvas` + `TextMeasurer` (`rememberTextMeasurer()`), Composable নেস্টিং নয়।

### 7.1 `DisplayNode` ট্রি

```kotlin
sealed interface DisplayNode {
    data class Row(val children: List<DisplayNode>) : DisplayNode
    data class Text(val text: String, val italic: Boolean = false) : DisplayNode
    data class Frac(val top: DisplayNode, val bottom: DisplayNode) : DisplayNode
    data class Sqrt(val radicand: DisplayNode) : DisplayNode          // অগ্রভাগে √ + উপরে দাগ
    data class NthRoot(val index: DisplayNode, val radicand: DisplayNode) : DisplayNode
    data class Pow(val base: DisplayNode, val exp: DisplayNode) : DisplayNode  // base বন্ধনীসহ
    data class SubScript(val content: DisplayNode) : DisplayNode      // log-এর ভিত্তি, x= ইত্যাদি
    data class SupScript(val content: DisplayNode) : DisplayNode      // ঘাত, ×⒑ এর ঘাত
    data class Integral(val eq: DisplayNode, val lower: DisplayNode, val upper: DisplayNode) : DisplayNode
    data class Derivative(val eq: DisplayNode, val at: DisplayNode) : DisplayNode
    object Placeholder : DisplayNode                                   // ▯
    object Cursor : DisplayNode                                        // ব্লিংকিং
    object LineBreak : DisplayNode
}
```
Phase 4-এর রেন্ডার-পাস সরাসরি এই ট্রি বানাবে (HTML স্ট্রিং বানানোর দরকার নেই)।

### 7.2 মেট্রিক্স (CSS থেকে সরাসরি নেওয়া)

**রুট ফন্ট সাইজ** (মূল কোডের `handle_resize()` থেকে):
```
rootFontSizePx = displayOutputRectHeightPx * 0.034506 * 16.91331 / 2
               ≈ displayOutputRectHeightPx * 0.29180534
```
`1rem = rootFontSizePx`। বাকি সব:

| CSS ক্লাস | মান | মানে |
|---|---|---|
| বেস টেক্সট | `1rem` | সাধারণ লেখা |
| `.frac_top`, `.frac_bottom` | `font-size: 0.667rem` | **নেস্টেড হলেও আর ছোট হয় না** — সব লেভেলে 0.667rem |
| `.frac_bottom` | `border-top: 0.111rem`, `padding-top: 0.111rem`, `margin-top: 0.111rem` | ভগ্নাংশের দাগ ও ফাঁক |
| `.frac_top`/`.frac_bottom` | `padding-left: 0.111rem` | |
| `.frac_wrapper` | `margin-right: 0.111rem`, `line-height: 1`, লেখা কেন্দ্রে | |
| `.pow_top` | `font-size: 0.667rem`, `vertical-align: 0.667em` | সূচক উপরে |
| `.pow_bottom` | `padding-left: 0.2rem` | ভিত্তির বাঁ পাশে ফাঁক |
| `.logn_bottom` | `font-size: 0.667rem`, `vertical-align: -0.333em` | নিম্নলিপি |
| `.sqrt` | `border-top: 0.111em`, `padding-right: 0.222em`, `padding-top: 0.111em` | ⚠️ `em` — **বর্তমান** ফন্ট সাইজের সাপেক্ষে |
| `.integ_top`, `.integ_bottom` | `font-size: 0.667rem` | সীমা |
| `.cursor` | `width: 0`, `height: 1em`, ব্লিংক ১ সেকেন্ড (০–৫০% দৃশ্যমান, ৫১–১০০% অদৃশ্য) | প্রস্থ শূন্য! |
| `#math-input` | `padding-top: 0.222rem`, `padding-left: 0.111rem`, `white-space: nowrap` | |
| `#math-output` | `padding-left: 0.111rem`, flex-wrap, নিচে-ডানে অ্যালাইন | |

**রঙ:** ডিসপ্লের সব লেখা ও দাগ `#3f4266` (ডিজাইন JSON-এর `font_color`)।
**ফন্ট:** `schulrechner_regular`; ভেরিয়েবলের নাম (`x`, `μ`, `π`, `α` …) ইটালিক।

### 7.3 বিশেষ লেআউট নিয়ম

**ক) ভগ্নাংশের উল্লম্ব অবস্থান** (`vertical_align_elements()` এর নেটিভ রূপ)
ওয়েবে প্রতিটা `frac_wrapper`-এর `vertical-align` এমনভাবে সেট হয় যাতে —
> **ভগ্নাংশের দাগটা (frac_bottom-এর উপরের বর্ডার) আশপাশের লেখার উল্লম্ব মধ্যবিন্দুতে বসে**,
> আর তার সাথে `+ 0.06rem` অতিরিক্ত অফসেট যোগ হয়।

নেটিভে: `Frac` নোডের ব্যাসলাইন-অফসেট এমন দাও যাতে ভগ্নাংশের দাগ
`parentLineCenterY + 0.06rem` এ পড়ে। নেস্টেড ভগ্নাংশে ভেতর থেকে বাইরে (গভীরতম আগে) হিসাব করো
— মূল কোডও `alignRight<id>` গুলো **আইডি অনুযায়ী অবরোহী ক্রমে** প্রসেস করে, মানে
পরে-তৈরি (ভেতরের) ভগ্নাংশ আগে।

**খ) `√` ও `∫` গ্লিফ টানা** (`scale_height_elements()`)
```
scaleY = parentHeightPx / glyphNaturalHeightPx
```
মানে √ চিহ্নটা তার ভেতরের রাশির উচ্চতা অনুযায়ী উল্লম্বভাবে **টেনে লম্বা** করা হয়
(অনুভূমিকভাবে নয়)। Compose-এ: `drawContext.canvas.scale(1f, scaleY)` বা
`TextStyle`-এর বদলে `Path` ট্রান্সফর্ম।
√-এর ভেতরের রাশির **উপরে একটা অনুভূমিক দাগ** (`border-top: 0.111em`) আর ডানে `0.222em` ফাঁক।

**গ) ঘাতের ভিত্তি**: `Pow` সবসময় ভিত্তিকে **আক্ষরিক বন্ধনীসহ** দেখায় — `(150238)²`।
স্ক্রিনশটে যেমন `(A/2)²`।

**ঘ) ∫-এর বিন্যাস**: বাঁ থেকে ডানে → `∫` গ্লিফ, তারপর সীমাগুলো উল্লম্বভাবে
(**উপরে = দ্বিতীয়বার টাইপ করা সীমা, নিচে = প্রথমবার টাইপ করা সীমা**), তারপর রাশি, শেষে `dX`।

**ঙ) d/dx-এর বিন্যাস**: `d/dx` (ছোট ভগ্নাংশ) `(` রাশি `)` `|` `ₓ₌` মান

### 7.4 ইনপুট / আউটপুট এরিয়া

| | ইনপুট এরিয়া | আউটপুট এরিয়া |
|---|---|---|
| জায়গা | `CalcGeometry.DISPLAY_INPUT` | `CalcGeometry.DISPLAY_OUTPUT` |
| অ্যালাইনমেন্ট | উপরে-বাঁয়ে | **নিচে-ডানে** |
| লাইন | এক লাইন (`nowrap`), স্ক্রল হয় | wrap হতে পারে (কমপ্লেক্সে দুই লাইন) |
| ক্লিপিং | overflow hidden | overflow hidden |

দুটো এরিয়া SVG-তে **ওভারল্যাপ করে** — এটা ইচ্ছাকৃত: LCD-র পুরোটাই দুজনে ভাগ করে নেয়,
ইনপুট উপর থেকে বাড়ে, আউটপুট নিচ থেকে।

### 7.5 স্ক্রলিং (`scroll_element()` এর নেটিভ রূপ)

কার্সর যেন সবসময় দেখা যায় — প্রতিবার রেন্ডারের পর:
```
dx = cursorRightX - SCROLL_X_BORDER.left        // ভিউপোর্ট কোঅর্ডিনেটে
dy = cursorBottomY - SCROLL_Y_BORDER.top
scrollX = (scrollX + dx).coerceIn(0f, maxScrollX)
scrollY = (scrollY + dy).coerceIn(0f, maxScrollY)
```
`SCROLL_X_BORDER`/`SCROLL_Y_BORDER` হলো SVG-তে থাকা অদৃশ্য রেফারেন্স রেক্ট (Phase 2-এ পেয়েছ)।
> কার্সর না থাকলে (ফল দেখানোর সময়) স্ক্রল `0,0` তে রিসেট হয় (`math_input_element.scroll(0,0)`)।

### টাস্ক
- [ ] **7.1** `DisplayNode` + Phase 4-এর রেন্ডার পাস সরাসরি এই ট্রি বানাবে
- [ ] **7.2** `MathLayout` — রিকার্সিভ measure: প্রতিটা নোডের `width`, `ascent`, `descent`
- [ ] **7.3** `MathCanvas` — draw পাস, `TextMeasurer` দিয়ে গ্লিফ, `drawLine` দিয়ে ভগ্নাংশ/রুটের দাগ
- [ ] **7.4** কার্সর ব্লিংক: `rememberInfiniteTransition`, ১০০০ms, ৫০% ডিউটি সাইকেল, keyframe (স্মুথ ফেড নয়)
- [ ] **7.5** স্ক্রল স্টেট + ক্লিপিং
- [ ] **7.6** `@Preview` Composable: `-A/2+√((A/2)²-B)` — স্ক্রিনশট ১-এর মতো দেখাতে হবে

### DoD
- [ ] `phoneScreenshots/3.jpg`-এর রাশিটা (`(5+17)/(5×⒑²) − sin(45)/(√7 log₃(√(30π)))`) হুবহু রেন্ডার হয়
- [ ] নেস্টেড ভগ্নাংশে দাগগুলো ঠিক জায়গায় (ছোট হয় না, কেন্দ্রে বসে)
- [ ] লম্বা রাশিতে কার্সর স্ক্রল করে দেখা যায় (X ও Y দুদিকেই)
- [ ] খালি স্লটে `▯` দেখায় এবং কার্সর তার **আগে** বসে

---

# Phase 8 — UI Shell

### 8.1 রুট লেআউট

```kotlin
Box(Modifier.fillMaxSize().background(Color.Black)) {          // চারপাশে কালো
    Box(
        Modifier.align(Alignment.Center)
                .aspectRatio(2486.6667f / 4912f)               // = 0.50625
                .fillMaxSize()                                  // "fit" আচরণ
    ) {
        CalculatorBody()      // VectorDrawable স্ট্যাক
        DisplayLayer()        // ইনপুট + আউটপুট (Phase 7)
        IndicatorLayer()
        KeypadLayer()         // স্বচ্ছ, শুধু টাচ ধরে
        OverlayLayer()        // সেটিংস / চেঞ্জলগ / অ্যালার্ট
    }
}
```
SVG ওয়েবে `preserveAspectRatio` ডিফল্ট (`xMidYMid meet`) মানে **fit + center** — উপরেরটা তার সমতুল্য।

### 8.2 বডি রেন্ডার (স্ট্যাক করার ক্রম, নিচ থেকে উপরে)

```
1. calc_body.xml                       (কেসিং, LCD, সব লেখা)
2. keybg_<code>.xml  × ৫০              (সব বাটনের ব্যাকগ্রাউন্ড)
3. label_comma_de.xml বা label_comma_en.xml  (লোকেল অনুযায়ী একটাই)
4. ডিসপ্লে (ইনপুট + আউটপুট টেক্সট)
5. ind_<name>.xml                      (শুধু সক্রিয়গুলো)
```
সবগুলোর viewport একই (`2486.6667 × 4912`) — তাই `Modifier.fillMaxSize()` দিলেই নিখুঁতভাবে বসবে।

### 8.3 কী হিট-টেস্ট (⚠️ খুব সাবধানে)

**bounding box দিয়ে করলে ভুল হবে।** REPLAY প্যাডের চারটা তীর (`key_dir0..key_dir3`)
আসলে চারটা বাঁকা ত্রিভুজ-মতো আকৃতি, যাদের bounding box একে অন্যের ওপর অনেকখানি ওভারল্যাপ করে।

```kotlin
// একবার তৈরি করে ক্যাশ করো
val regions: List<Pair<String, Region>> = KEY_AREAS.map { area ->
    val path = PathParser().parsePathString(area.pathData).toPath().asAndroidPath()
    val r = RectF(); path.computeBounds(r, true)
    area.code to Region().apply { setPath(path, Region(Rect(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt()))) }
}

fun hitTest(vx: Int, vy: Int): String? =
    regions.asReversed().firstOrNull { it.second.contains(vx, vy) }?.first
//           ^^^^^^^^^^^ SVG ডকুমেন্টে **পরে** থাকা এলিমেন্ট **উপরে** থাকে
```
টাচ পয়েন্টকে স্ক্রিন px → ভিউপোর্ট কোঅর্ডিনেটে রূপান্তর করো:
`vx = (touchX / boxWidth) * 2486.6667` , `vy = (touchY / boxHeight) * 4912`।

**ইভেন্ট টাইপ:** `pointerDown`-এই কী ফায়ার করবে (`pointerUp` নয়) — মূল অ্যাপ `pointerdown` ব্যবহার করে।
`Modifier.pointerInput { awaitPointerEventScope { … awaitFirstDown() … } }` ব্যবহার করো।

### 8.4 প্রেস অ্যানিমেশন

মূল CSS: `.pressed { filter: brightness(85%); transition: transform .1s, filter .1s; }`
JS: ক্লাস যোগ, **১৫০ms** পরে সরানো।

```kotlin
// চাপা কী-টার keybg আবার আঁকো, ৮৫% brightness-এ
val darken = ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.85f, 0.85f, 0.85f, 1f) })
```
`pointerDown` এ শুরু, ঠিক ১৫০ms পরে শেষ (আঙুল তোলার সাথে সম্পর্ক নেই)।

### 8.5 ইন্ডিকেটর

| ইন্ডিকেটর | কখন জ্বলে |
|---|---|
| `indicator_shift` | SHIFT মোড চালু |
| `indicator_alpha` | ALPHA মোড চালু |
| `indicator_STO` | STO মোড চালু |
| `indicator_cmplx` | `calcMode == CMPLX` |
| `indicator_fix` | `roundingMode` `Fix_*` |
| `indicator_deg` / `indicator_rad` / `indicator_gra` | কোণের একক |
| বাকি ১০টা (`M`, `RCL`, `sci`, `math`, `mat`, `vct`, `stat`, `disp`, `delta`, `nabla`) | **কখনো নয়** (মূল অ্যাপেও অব্যবহৃত) — ড্রয়েবল রাখো, দেখাবে না |

### 8.6 সেটিংস প্যানেল

মূল অ্যাপে: নিচে-ডানে ছোট ⚙️ বাটন → একটা ডার্ক প্যানেল খোলে।
হুবহু একই দেখতে বানাও:

| প্রপার্টি | মান |
|---|---|
| ব্যাকগ্রাউন্ড | `#1e1e2f` |
| লেখা | `#ffffff` |
| কোণা | `0.75rem` রেডিয়াস |
| চেকবক্স accent | `#696982` |
| ড্রপডাউন/বোতাম bg | `#2c2c3e` |
| ⚙️ বাটন | `#1e1e2f` bg, নিচে-ডানে `0.1rem`, রেডিয়াস `0.25rem` |
| ফন্ট | সিস্টেম ফন্ট (LCD ফন্ট নয়) |

**কনটেন্ট (এই ক্রমে):**
```
Settings
  Decimal Format:  [ π = 3.14159...  |  π = 3,14159... ]   ← ড্রপডাউন
  Prefer Decimal Numbers:    [x]
  Turn Off = Close App:      [x]
  ───────────────────────────────
  MK Shaon
    GitHub    →  https://github.com/mkshaonexe
    Website   →  https://mkshaon.com
    Email     →  mkshaondev@gmail.com
  ───────────────────────────────
  Open Source Licenses       →  (নতুন শিট, §8.7)
                                            [ Close ]
```
- ❌ **বাদ যাবে:** Design select, Liberapay/Donate, License লিংক (মূল রেপোর),
  Web Version, WebSite (মূল লেখকের), SourceCode, IssueTracker, F-Droid।
- Decimal Format বা Design বদলালে ওয়েব ভার্সন `location.reload()` করে —
  নেটিভে `viewModel.resetAll()` (Activity রিক্রিয়েট নয়)।
- লিংক খুলবে: `startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))`
  ইমেইল: `Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mkshaondev@gmail.com"))`
  দুটোই `try/catch (ActivityNotFoundException)` দিয়ে মোড়াও।

### 8.7 Licenses শিট (বাধ্যতামূলক — §3 দেখো)

```
Open Source Licenses

My Calculator
  © 2026 MK Shaon — GPL-3.0-only
  https://github.com/mkshaonexe

Calculator artwork & display font
  © Joris Yidong Scholl
  Artwork: CC BY-SA 4.0 · Font: GPL-3.0-only
  Original project: https://github.com/CardiJey/schulrechner

This application is free software licensed under the
GNU General Public License v3.0. It comes with ABSOLUTELY
NO WARRANTY.
```
রিপোর রুটে `LICENSE` (GPL-3.0 ফুল টেক্সট) আর `NOTICE.md` ফাইল দুটোও রাখো।

### 8.8 "What's new" ওভারলে

- ভার্সন নম্বর স্ক্রিনের **নিচে-বাঁয়ে** ছোট করে (`0.6rem`) দেখাবে; ট্যাপ করলে ওভারলে টগল।
- ওভারলে: পুরো স্ক্রিন, `rgba(0,0,0,0.8)` ব্যাকগ্রাউন্ড, সাদা লেখা, `3vw` প্যাডিং, স্ক্রলযোগ্য।
- লাইনের রঙ: `- Feature:` → সবুজ `#00ff00`, `- Tweak:` → ধূসর `gray`, `- Bugfix:` → সাদা।
- `lastSeenVersionCode != BuildConfig.VERSION_CODE` হলে **অ্যাপ চালু হওয়ার সময় একবার নিজে থেকে খুলবে**,
  তারপর মান সেভ হবে।
- চেঞ্জলগ টেক্সট `res/raw/changelog.txt`-এ রাখো (v1.0 = `- Feature: First release`)।

### 8.9 অ্যালার্ট ডায়ালগ

মূল অ্যাপে একটাই অ্যালার্ট: অ্যাপ বন্ধ করতে না পারলে।
```
Can not close app!
The platform does not allow closing the app from within.
Disable closing?
            [ Cancel ]   [ Okay ]
```
`Okay` → `turnOffClose = false` সেভ করে reset।
> Android-এ `finishAndRemoveTask()` প্রায় সবসময় কাজ করে, তাই এই ডায়ালগ কদাচিৎ লাগবে —
> তবু ইমপ্লিমেন্ট করে রাখো (মূল আচরণের অংশ)।

### 8.10 ব্যাক বাটন

```
সেটিংস/চেঞ্জলগ/অ্যালার্ট খোলা থাকলে → সেটা বন্ধ করো
নইলে → অ্যাপ থেকে বেরিয়ে যাও (ডিফল্ট)
```
`BackHandler` ব্যবহার করো।

### DoD
- [ ] `phoneScreenshots/2.jpg`-এর ক্যালকুলেটরটার সাথে পাশাপাশি রেখে তুলনা করলে পার্থক্য ধরা যায় না
- [ ] REPLAY প্যাডের চারটা তীর আলাদা আলাদা কাজ করে (ওভারল্যাপ সমস্যা নেই)
- [ ] প্রতিটা বাটনে চাপলে সেটাই একটু গাঢ় হয়, ১৫০ms পরে স্বাভাবিক
- [ ] ⚙️ → সেটিংস → শুধু MK Shaon-এর তথ্য দেখায়, লিংক খোলে
- [ ] কোনো নেটওয়ার্ক পারমিশন ছাড়াই ব্রাউজার/মেইল খোলে

---

# Phase 9 — টেস্ট

### 9.1 গোল্ডেন টেস্ট পোর্ট করা (সবচেয়ে গুরুত্বপূর্ণ)

`schulrechner/tests/input_to_output_jsons/` থেকে **১০১টা কেস** (`test_batch_1.json`-এ ১০০ +
`fraction_2.json`-এ ১) কপি করে আনো:

```
app/src/test/resources/golden/test_batch_1.json
app/src/test/resources/golden/fraction_2.json
app/src/test/resources/golden/known_to_fail/not_fraction_1.json
app/src/test/resources/golden/known_to_fail/not_fraction_2.json
```

প্রতিটা কেসের গঠন:
```json
{
  "userLang": "en-US",             // বা "de-DE"
  "name": "...",
  "input_history": ["key_frac","key_1", ...],
  "rendered_input":  "<span …>…</span> ",
  "rendered_output": "<span …>…</span>",
  "prefer_decimals": false,        // ঐচ্ছিক, ডিফল্ট false
  "calc_mode": "COMP",             // ঐচ্ছিক, ডিফল্ট COMP
  "rounding_mode": "Norm_1",       // ঐচ্ছিক, ডিফল্ট Norm_1
  "angle_mode": "Deg",             // ঐচ্ছিক, ডিফল্ট Deg
  "turn_off_close": false          // ঐচ্ছিক, ডিফল্ট false
}
```

**কিভাবে তুলনা করবে:** expected মানগুলো HTML। তাই একটা ছোট **HTML → `DisplayNode`** কনভার্টার
লেখো (শুধু টেস্ট সোর্স সেটে, `src/test/`-এ), তারপর দুটো ট্রি তুলনা করো।
নরমালাইজেশন (মূল হারনেসের মতো): `"` → `'`, আর সব ` style='…'` অ্যাট্রিবিউট মুছে ফেলো।

HTML সাবসেটটা খুবই ছোট — এই ম্যাপিংটাই যথেষ্ট:

| HTML | DisplayNode |
|---|---|
| `<span class='frac_wrapper'><span class='frac_top'>A</span><span class='frac_bottom …'>B</span></span>` | `Frac(A, B)` |
| `<span class='sqrt_wrapper'><span class='scale_height'>√</span><span class='sqrt'>A</span></span>` | `Sqrt(A)` |
| `<span class='pow_bottom'>(A)</span><span class='pow_top'>B</span>` | `Pow(A, B)` |
| `<span class='pow10'>×⒑</span>` | `Text("×⒑")` |
| `<span class='pow_top'>N</span>` | `SupScript(N)` |
| `<span class='logn_bottom'>A</span>` | `SubScript(A)` |
| `<span class='integ_*'>…` | `Integral(...)` |
| `<span class='alignLeftN'></span>` | (উপেক্ষা) |
| `<span class='cursor'></span>` | `Cursor` |
| `<i>x</i>` | `Text("x", italic = true)` |
| `<br>` | `LineBreak` |
| `&nbsp;` / ` ` | `Text(" ")` |
| খালি টেক্সট / `▯` | `Text` / `Placeholder` |

**দুই ধাপে করো** (এতে আটকে থাকবে না):
- **ধাপ ১ (আগে করো):** যেসব কেসে `rendered_output`-এ কোনো ট্যাগ নেই (প্লেইন সংখ্যা) —
  শুধু আউটপুট স্ট্রিং মেলাও। ~৫০টা কেস এভাবেই পাস করবে; এতেই গণিত ইঞ্জিন প্রমাণিত হবে।
- **ধাপ ২:** বাকি সব + `rendered_input`-এর ট্রি তুলনা।

`known_to_fail/` এর দুটো কেস `@Ignore` দিয়ে রাখো (মূল রেপোতেও `test.todo`)।

### 9.2 অতিরিক্ত ইউনিট টেস্ট (নিজে লিখো)

- [ ] `MathEngineTest` — Phase 3-এর DoD তালিকা
- [ ] `NumberFormatterTest` — Phase 6-এর টেবিল
- [ ] `TreeBuilderTest` — Phase 4-এর DoD তালিকা
- [ ] `ModeMapTest` — Appendix B-র প্রতিটা ম্যাপিং
- [ ] `ConstantsTest` — Appendix D-র ৪৩টা ধ্রুবকের মান
- [ ] `SexagesimalRewriterTest` — §3.6-এর ১০টা রুল
- [ ] `ContinuedFractionTest` — 0.5→1/2, 19/6, π→ভগ্নাংশ নয়

### 9.3 UI টেস্ট (`androidTest`)

- [ ] `KeyHitTestTest` — REPLAY প্যাডের চারটা তীরের কেন্দ্রে ট্যাপ করলে সঠিক কী ফায়ার হয়
- [ ] `CalculatorScreenTest` — `7 × 8 =` → `56` দেখায়
- [ ] `SettingsTest` — ⚙️ → প্যানেল খোলে → চেকবক্স টগল → সেভ হয়
- [ ] স্ক্রিনশট টেস্ট (ঐচ্ছিক কিন্তু ভালো): রেফারেন্স স্ক্রিনশটের সাথে তুলনা

### 9.4 ম্যানুয়াল চেকলিস্ট (মূল রেপোর `manual_test_results.csv` থেকে)

প্রতিটা রিলিজে হাতে যাচাই করো:
```
[ ] ui_loads_test                  UI ঠিকমতো লোড হয়
[ ] buttons_work_test              সব বাটন কাজ করে
[ ] overflow_scrolling_x_test      লম্বা রাশিতে আড়াআড়ি স্ক্রল
[ ] overflow_scrolling_y_test      উঁচু রাশিতে উল্লম্ব স্ক্রল
[ ] nested_fractions_display_test  ভগ্নাংশের ভেতর ভগ্নাংশ ঠিক দেখায়
[ ] brackets_display_test          বন্ধনী ঠিক দেখায়
[ ] sqrt_display_test              √ চিহ্ন উচ্চতা অনুযায়ী টানে
[ ] urls_work                      সেটিংসের লিংক খোলে
[ ] hotkeys_work                   হার্ডওয়্যার কীবোর্ড কাজ করে
```

### DoD
```bash
./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest
```
- [ ] ১০১টার মধ্যে **১০১টাই** পাস (known_to_fail বাদে)
- [ ] কোনো টেস্ট ফ্লেকি নয়

---

# Phase 10 — বিল্ড, ভেরিফিকেশন, ফাইনাল পলিশ

### টাস্ক
- [ ] **10.1** `./gradlew :app:assembleDebug :app:assembleRelease` — দুটোই সফল
- [ ] **10.2** ইমুলেটর/ডিভাইসে ইনস্টল করে **হাতে চালাও** — Phase 9.4-এর পুরো চেকলিস্ট
- [ ] **10.3** রেফারেন্স স্ক্রিনশটের পাশে রেখে পিক্সেল-তুলনা; পার্থক্য থাকলে ঠিক করো
- [ ] **10.4** পারফরম্যান্স:
      - কী-প্রেস → ডিসপ্লে আপডেট < ১৬ms (পুরো হিস্ট্রি রি-প্লে সত্ত্বেও)
      - ৫০+ কী-প্রেসের লম্বা রাশিতেও jank নেই
      - `∫` হিসাব UI ব্লক করে না
      - অ্যাপ স্টার্টআপ < ১ সেকেন্ড
- [ ] **10.5** বিভিন্ন স্ক্রিনে দেখো: ছোট ফোন (৫″), বড় ফোন (৬.৮″), ট্যাবলেট, ফোল্ডেবল —
      সব জায়গায় ক্যালকুলেটর কেন্দ্রে, aspect ঠিক, কালো পটভূমি
- [ ] **10.6** ডার্ক মোড: অ্যাপ সবসময় নিজের চেহারায় থাকবে (সিস্টেম থিম প্রভাব ফেলবে না)
- [ ] **10.7** রোটেশন লক (পোর্ট্রেট) ঠিকমতো কাজ করছে
- [ ] **10.8** প্রসেস-ডেথের পর স্টেট ফেরে (`rememberSaveable` / `SavedStateHandle`)
- [ ] **10.9** `LICENSE`, `NOTICE.md`, `README.md` লেখো
- [ ] **10.10** `versionName = "1.0"`, `versionCode = 1`; চেঞ্জলগ ফাইল ঠিক আছে
- [ ] **10.11** APK সাইজ দেখো (< ১০ MB হওয়া উচিত); ProGuard ঠিকমতো চলছে
- [ ] **10.12** `.gitignore` ঠিক করো, ফাইনাল কমিট

### চূড়ান্ত একসেপ্টেন্স (এগুলোর একটাও না হলে কাজ শেষ নয়)

```
[ ] অ্যাপে একটাও WebView / HTML / JS / Cordova নেই
[ ] ১০১টা গোল্ডেন টেস্ট পাস
[ ] সব ৫০টা বাটন সঠিক জায়গায়, সঠিক কাজ করে
[ ] SHIFT / ALPHA / STO তিনটা মোড ও তাদের পুরো ম্যাপ কাজ করে
[ ] MODE (COMP/CMPLX) ও SETUP (Deg/Rad/Gra/Fix/Norm) মেনু কাজ করে, সেটিং টেকে
[ ] CONST 01–40 সব ধ্রুবক সঠিক মান দেয়
[ ] hyp মেনুর ৬টা ফাংশন কাজ করে
[ ] ভগ্নাংশ, √, ⁿ√, ঘাত, logₙ, d/dx, ∫ — সবগুলো লেখা যায়, দেখা যায়, হিসাব হয়
[ ] S⇔D, ENG, ENG←, °′‴ টগল কাজ করে
[ ] Ans, A–F, X, Y, M, M+, M− কাজ করে
[ ] ▲/▼ দিয়ে আগের হিসাব রিপ্লে ও এডিট করা যায়
[ ] ON/OFF আচরণ ও "Turn Off = Close App" সেটিং কাজ করে
[ ] error অবস্থায় ইনপুট-এরিয়ায় "error", আউটপুট খালি
[ ] সেটিংসে শুধু MK Shaon-এর তথ্য
[ ] Licenses স্ক্রিন আছে, GPL-3.0 + CC BY-SA 4.0 অ্যাট্রিবিউশনসহ
[ ] কোনো পারমিশন নেই, কোনো নেটওয়ার্ক কল নেই
[ ] সব ডিভাইস সাইজে ঠিক দেখায়
```

---
---

# 📎 Appendix A — সম্পূর্ণ কী-ইনভেন্টরি (৫০টা)

SVG-তে ঠিক **৫০টা** `key_*` এলিমেন্ট আছে। প্রতিটার জন্য একই নামের একটা `label_background_*` আছে
(প্রেস অ্যানিমেশনের জন্য)। "বেস" = কোনো মোড ছাড়া, "SHIFT/ALPHA/STO" = মোড চেপে।

| key id | বেস কাজ | SHIFT | ALPHA | STO |
|---|---|---|---|---|
| `key_shift` | SHIFT মোড টগল | — | — | — |
| `key_alpha` | ALPHA মোড টগল | — | — | — |
| `key_mode` | MODE মেনু | SETUP মেনু | — | — |
| `key_on` | অ্যাপ রিস্টার্ট | — | — | — |
| `key_dir0` | কার্সর ◀ | — | — | — |
| `key_dir1` | কার্সর ▼ / রিপ্লে | — | — | — |
| `key_dir2` | কার্সর ▶ | — | — | — |
| `key_dir3` | কার্সর ▲ / রিপ্লে | — | — | — |
| `key_calc` | **কিছুই না** (অব্যবহৃত) | — | — | — |
| `key_integ` | `∫ … dX` | `d/dx` | — | — |
| `key_pow_minus1` | `x⁻¹` | `x!` (ফ্যাক্টোরিয়াল) | — | — |
| `key_logn` | `log_n(…)` | — | — | — |
| `key_frac` | ভগ্নাংশ | — | — | — |
| `key_sqrt` | `√` | `∛` (ঘনমূল) | — | — |
| `key_pow2` | `x²` | `x³` | — | — |
| `key_pown` | `x^▯` | `ⁿ√` | — | — |
| `key_log` | `log(` (ভিত্তি ১০) | — | — | — |
| `key_ln` | `ln(` | `e^▯` | — | — |
| `key_neg` | `(−)` ঋণাত্মক | — | ভেরিয়েবল `A` | `→A` |
| `key_deg` | `°` (DMS ইনপুট) / আউটপুট টগল | — | ভেরিয়েবল `B` | `→B` |
| `key_hyp` | hyp মেনু | — | ভেরিয়েবল `C` | `→C` |
| `key_sin` | `sin(` | `sin⁻¹(` | ভেরিয়েবল `D` | `→D` |
| `key_cos` | `cos(` | `cos⁻¹(` | ভেরিয়েবল `E` | `→E` |
| `key_tan` | `tan(` | `tan⁻¹(` | ভেরিয়েবল `F` | `→F` |
| `key_rcl` | **কিছুই না** | **STO মোড চালু** | — | — |
| `key_eng` | ENG ফরম্যাট (level +1) | ENG← (level −1) | — | — |
| `key_lparen` | `(` | `%` (= 0.01) | — | — |
| `key_rparen` | `)` | — | ভেরিয়েবল `X` | `→X` |
| `key_SD` | S⇔D (ভগ্নাংশ ⇄ দশমিক) | — | ভেরিয়েবল `Y` | `→Y` |
| `key_M_plus` | `M+` | `M−` | ভেরিয়েবল `M` | `→M` |
| `key_7` | সংখ্যা 7 | **CONST মেনু** | — | — |
| `key_8` `key_9` | সংখ্যা | — | — | — |
| `key_del` | DEL (এক ধাপ মুছুন) | — | — | — |
| `key_ac` | AC (সব মুছুন) | **OFF** | — | — |
| `key_4` `key_5` `key_6` | সংখ্যা | — | — | — |
| `key_x` | `×` | — | — | — |
| `key_div` | `÷` | — | — | — |
| `key_1` `key_2` `key_3` | সংখ্যা | — | — | — |
| `key_plus` | `+` | — | — | — |
| `key_minus` | `−` | — | — | — |
| `key_0` | সংখ্যা 0 | — | — | — |
| `key_comma` | দশমিক বিন্দু (`.` বা `,`) | — | — | — |
| `key_pow10` | `×10ˣ` | `π` | `e` (অয়লার) | — |
| `key_Ans` | `Ans` | — | — | — |
| `key_equals` | `=` | — | — | — |

**CMPLX মোডে অতিরিক্ত:** `key_eng` → `i` (কাল্পনিক একক)।
**শুধু hyp মেনু থেকে পাওয়া যায়:** `sinh cosh tanh sinh⁻¹ cosh⁻¹ tanh⁻¹`।

---

# 📎 Appendix B — মোড-ম্যাপ (`Classic_by_Joris Yidong Scholl.json` থেকে হুবহু)

```kotlin
val SHIFT_MAP = mapOf(
    "key_pow10"      to "key_pi",
    "key_rcl"        to "key_STO",          // বিশেষ: এলিমেন্ট নয়, STO মোড চালু করে
    "key_sqrt"       to "key_sqrt3",
    "key_pow2"       to "key_pow3",
    "key_pown"       to "key_sqrtn",
    "key_pow_minus1" to "key_faculty",
    "key_ln"         to "key_epow",
    "key_sin"        to "key_sin_minus1",
    "key_cos"        to "key_cos_minus1",
    "key_tan"        to "key_tan_minus1",
    "key_M_plus"     to "key_M_minus",
    "key_7"          to "key_CONST",
    "key_integ"      to "key_deriv",
    "key_mode"       to "key_setup",
    "key_eng"        to "key_back",
    "key_ac"         to "key_off",
    "key_lparen"     to "key_perc",
)

val ALPHA_MAP = mapOf(
    "key_pow10"  to "key_e",
    "key_neg"    to "key_uservar_A",
    "key_deg"    to "key_uservar_B",
    "key_hyp"    to "key_uservar_C",
    "key_sin"    to "key_uservar_D",
    "key_cos"    to "key_uservar_E",
    "key_tan"    to "key_uservar_F",
    "key_rparen" to "key_uservar_X",
    "key_SD"     to "key_uservar_Y",
    "key_M_plus" to "key_uservar_M",
)

val STO_MAP = mapOf(
    "key_neg"    to "key_STO_A",
    "key_deg"    to "key_STO_B",
    "key_hyp"    to "key_STO_C",
    "key_sin"    to "key_STO_D",
    "key_cos"    to "key_STO_E",
    "key_tan"    to "key_STO_F",
    "key_rparen" to "key_STO_X",
    "key_SD"     to "key_STO_Y",
    "key_M_plus" to "key_STO_M",
)

val CMPLX_MAP = mapOf(
    "key_eng" to "key_i",
)
```
> `CMPLX_MAP` অন্যগুলোর মতো নয় — এটা `ComplexEquationHandler.handle()`-এ **সবার আগে** প্রয়োগ হয়,
> shift/alpha/STO দেখার আগেই।
>
> ডিসপ্লের লেখার রঙ: `font_color = "#3f4266"`।

---

# 📎 Appendix C — KeyCode → এলিমেন্ট/অ্যাকশন টেবিল

| KeyCode | তৈরি হয় | kind | ডিসপ্লে | expr টুকরো |
|---|---|---|---|---|
| `key_0`…`key_9` | `Int` | Int | `0`…`9` | `0`…`9` |
| `key_comma` | `Point` | PointOp | লোকেল সেপারেটর (`.`/`,`) | `.` |
| `key_pow10` | `Pow10` | PointOp | `×⒑` | `e` |
| `key_plus` | `Plus` | AdditiveOp | `+` | `+` |
| `key_minus`, `key_neg` | `Minus` | AdditiveOp | `-` | `-` |
| `key_x` | `Times` | MultiOp | `×` | `*` |
| `key_div` | `Div` | MultiOp | `÷` | `/` |
| `key_lparen` | `Brackets` | BracketsOp | `(` | `(` |
| `key_rparen` | `BracketsClose` | BracketsClose | `)` | `)` |
| `key_faculty` | `Faculty` | PointOp | `!` | `!` |
| `key_deg` | `Sexagesimal` | PointOp | `°` | `°` (পরে রিরাইট) |
| `key_sin` | `Sin` | BracketsOp | `sin(` | Deg:`sind(` Rad:`sin(` Gra:`sing(` |
| `key_cos` | `Cos` | BracketsOp | `cos(` | `cosd(`/`cos(`/`cosg(` |
| `key_tan` | `Tan` | BracketsOp | `tan(` | `tand(`/`tan(`/`tang(` |
| `key_sin_minus1` | `ASin` | BracketsOp | `sin⁻¹(` | Deg:`180/PI*asin(` Rad:`asin(` Gra:`200/PI*asin(` |
| `key_cos_minus1` | `ACos` | BracketsOp | `cos⁻¹(` | একই ধরনে `acos(` |
| `key_tan_minus1` | `ATan` | BracketsOp | `tan⁻¹(` | একই ধরনে `atan(` |
| `key_sinh`…`key_atanh` | `Sinh`… | BracketsOp | `sinh(` … `tanh⁻¹(` | `sinh(` … `atanh(` |
| `key_log` | `Log` | BracketsOp | `log(` | `log10(` |
| `key_ln` | `Ln` | BracketsOp | `ln(` | `log(` ⚠️ (math.js-এ `log` = ln) |
| `key_frac` | `Frac` | ContainerOp | ভগ্নাংশ | Phase 4 টেবিল |
| `key_sqrt` | `Sqrt` | ContainerOp | `√` | `nthRootComplex(2,[` … |
| `key_sqrtn` | `Sqrtn` | ContainerOp | `ⁿ√` | `nthRootComplex([` … |
| `key_sqrt3` | `Sqrtn` + প্রি-ফিল `3` | ContainerOp | `³√` | একই |
| `key_pown` | `Pow(prefilled=false)` | ContainerOp | `(base)^exp` | `([` … |
| `key_pow2` | `Pow(prefilled=true)` + `2` | ContainerOp | `(base)²` | একই |
| `key_pow3` | `Pow(prefilled=true)` + `3` | ContainerOp | `(base)³` | একই |
| `key_pow_minus1` | `Pow(prefilled=true)` + `-1` | ContainerOp | `(base)⁻¹` | একই |
| `key_epow` | `Pow(prefilled=true)`, ভিত্তি = `e` | ContainerOp | `(e)^▯` | একই |
| `key_logn` | `Logx` | ContainerOp | `log_b(x)` | `(1/log([` … |
| `key_integ` | `Integrate` | ContainerOp | `∫ … dX` | subres |
| `key_deriv` | `Derivate` | ContainerOp | `d/dx(…)\|ₓ₌…` | subres |
| `key_i` | `CmplxI` | Var | `i` | `(i)` |
| `key_pi` | `Const(40)` | Var | `π` *(ইটালিক)* | `(3.141592653589793)` |
| `key_e` | `Const(41)` | Var | `e` *(ইটালিক)* | `(2.718281828459045)` |
| `key_perc` | `Const(42)` | Var | `%` | `(0.01)` |
| `key_CONST_NN` | `Const(NN-1)` | Var | Appendix D | Appendix D |
| `key_Ans` | `Ans` | Var | `Ans` | শেষ ফলাফল |
| `key_uservar_A`…`_M` | `UserVar` | Var | `A`…`M` | ভেরিয়েবলের মান (`X` হলে আক্ষরিক `X`) |
| `key_STO_A`…`_M` | `Sto` | Sto | `→A` | (কিছু না — শেষে কাজ করে) |
| `key_M_plus` / `key_M_minus` | `Memory` | Sto | `M+` / `M-` | (কিছু না) |
| `key_del` | — | — | ডিলিট (§4.8) | — |
| `key_ac` | — | — | সব মুছুন | — |
| `key_equals` | — | — | হিসাব করো | — |
| `key_dir0`…`key_dir3`, `pos1`, `end` | — | — | কার্সর মুভমেন্ট (§4.7) | — |
| `key_on` | — | — | অ্যাপ রিস্টার্ট | — |
| `key_off` | — | — | OFF স্ক্রিন / অ্যাপ বন্ধ | — |
| `key_SD`, `key_eng`, `key_back` | — | — | শুধু `EquationListHandler`-এ ফরম্যাট টগল | — |
| `key_CONST`, `key_hyp`, `key_mode`, `key_setup` | — | — | মেনু খোলে | — |
| `key_rcl`, `key_calc` | — | — | **কিছুই না** | — |

**কোণের একক (`angleMode`) কীভাবে খাটে:**
- `Deg`: `sin→sind`, `asin→180/PI*asin`
- `Rad`: কিছু বদলায় না
- `Gra`: `sin→sing`, `asin→200/PI*asin`
- হাইপারবোলিক ফাংশন কোণের একক **মানে না**।

---

# 📎 Appendix D — ধ্রুবকের তালিকা (CONST 01–40 + π, e, %)

`SHIFT` + `7` → `KONSTANTE / Nummer 01~40?` → দুই ডিজিট।
*(ইটালিক অক্ষরগুলো ডিসপ্লেতে ইটালিক ফন্টে আঁকতে হবে: μ, α, σ, ε, Φ, λ, γ, π, e)*

| # | চিহ্ন | মান |
|---|---|---|
| CONST 01 | `mP` | `1.672621637e-27` |
| CONST 02 | `mn` | `1.674927211e-27` |
| CONST 03 | `me` | `9.10938215e-31` |
| CONST 04 | `m`*μ* | `1.8835313e-28` |
| CONST 05 | `aO` | `5.291772086e-11` |
| CONST 06 | `h` | `6.62606896e-34` |
| CONST 07 | *μ*`N` | `5.05078324e-27` |
| CONST 08 | *μ*`B` | `9.27400915e-24` |
| CONST 09 | `ħ` | `1.054571628e-34` |
| CONST 10 | *α* | `7.297352538e-3` |
| CONST 11 | `re` | `2.817940289e-15` |
| CONST 12 | `λc` | `2.426310218e-12` |
| CONST 13 | `γP` | `267522209.9` |
| CONST 14 | `λCP` | `1.321409845e-15` |
| CONST 15 | `λCN` | `1.319590895e-15` |
| CONST 16 | `R∞` | `10973731.57` |
| CONST 17 | `u` | `1.660538782e-27` |
| CONST 18 | *μ*`P` | `1.410606662e-26` |
| CONST 19 | *μ*`e` | `-9.28476377e-24` |
| CONST 20 | *μ*`n` | `-9.6623641e-27` |
| CONST 21 | *μμ* | `-4.49044786e-26` |
| CONST 22 | `F` | `96485.3399` |
| CONST 23 | `e` | `1.602176487e-19` |
| CONST 24 | `NA` | `6.02214179e23` |
| CONST 25 | `k` | `1.3806504e-23` |
| CONST 26 | `Vm` | `0.022413996` |
| CONST 27 | `R` | `8.314472` |
| CONST 28 | `C0` | `299792458` |
| CONST 29 | `C1` | `3.74177118e-16` |
| CONST 30 | `C2` | `0.014387752` |
| CONST 31 | *σ* | `5.6704e-8` |
| CONST 32 | *ε*`0` | `8.854187817e-12` |
| CONST 33 | *μ*`0` | `1.256637061e-6` |
| CONST 34 | *Φ*`0` | `2.067833667e-15` |
| CONST 35 | `g` | `9.80665` |
| CONST 36 | `G0` | `7.7480917e-5` |
| CONST 37 | `Z0` | `376.7303134` |
| CONST 38 | `t` | `273.15` |
| CONST 39 | `G` | `6.67428e-11` |
| CONST 40 | `atm` | `101325` |
| (index 40) | *π* | `3.141592653589793` — `SHIFT`+`×10ˣ` |
| (index 41) | *e* | `2.718281828459045` — `ALPHA`+`×10ˣ` |
| (index 42) | `%` | `0.01` — `SHIFT`+`(` |

> ⚠️ `key_CONST_NN`-এ `NN` ১-ভিত্তিক, কিন্তু অ্যারে ইনডেক্স `NN − 1`।

---

# 📎 Appendix E — রঙের প্যালেট (SVG থেকে)

| রঙ | কোথায় |
|---|---|
| `#3f4266` | **LCD-র সব লেখা ও দাগ** (`font_color`) |
| `#7b8c87` | LCD-র সবুজাভ ব্যাকগ্রাউন্ড |
| `#2a2627` | ফাংশন বাটনের গাঢ় ডিম্বাকৃতি (৩০টা) |
| `#bfbdbf` | সংখ্যার বাটনের হালকা ব্যাকগ্রাউন্ড |
| `#df9300` | কমলা (DEL/AC, SHIFT লেবেল) |
| `#c15d1f` | গাঢ় কমলা — `label_background_del`, `label_background_ac` |
| `#555e6d` | `label_background_shift`, `_mode`, `_on`, `_alpha` |
| `#5b6d87` | প্যানেলের শেড |
| `#503c58` | প্যানেলের শেড |
| `#1c6159` | সবুজ লেবেল (ALPHA-র অক্ষর) |
| `#ff0018` | লাল লেবেল |
| `#4f2f2f` | উপরের সোলার-প্যানেল স্ট্রিপ |
| `#41424f` | ক্যালকুলেটরের পেছনের গাঢ় কেস |
| `#ffffff` / `#fcfcfc` | সাদা ডিটেইল |
| `#000000` | অ্যাপের পটভূমি (লেটারবক্স) |
| gradient `#777579` → `#cfced3` | কেসিংয়ের ধাতব গ্রেডিয়েন্ট |
| `#ff00c9` | ⚠️ শুধু অদৃশ্য হিট-এরিয়া — **কখনো আঁকবে না** |

সেটিংস প্যানেলের রঙ (CSS থেকে): `#1e1e2f` (bg), `#2c2c3e` (কন্ট্রোল), `#3d3d52` (hover),
`#696982` (accent/selected), `#ffffff` (লেখা)।

---

# 📎 Appendix F — কী-এর জ্যামিতি (SVG ভিউপোর্ট ইউনিট, `layer1` ট্রান্সফর্ম বেক করা)

viewport = `2486.6667 × 4912`। **ক্রমটাই SVG ডকুমেন্ট-অর্ডার — হিট-টেস্ট উল্টো দিক থেকে করবে।**
(এটা শুধু যাচাইয়ের জন্য; আসল উৎস = `CalcGeometry.kt`-তে থাকা পুরো path data।)

| # | key id | x0 | y0 | x1 | y1 |
|---|---|---|---|---|---|
| 1 | `key_shift` | 181.2 | 1831.3 | 553.0 | 2243.5 |
| 2 | `key_mode` | 1623.9 | 1793.8 | 1958.1 | 2226.2 |
| 3 | `key_on` | 1950.2 | 1778.3 | 2330.0 | 2226.7 |
| 4 | `key_alpha` | 553.9 | 1834.4 | 895.3 | 2244.5 |
| 5 | `key_integ` | 566.8 | 2260.2 | 881.4 | 2536.8 |
| 6 | `key_calc` | 199.5 | 2263.1 | 555.7 | 2545.9 |
| 7 | `key_pow_minus1` | 1621.8 | 2230.5 | 1947.1 | 2539.3 |
| 8 | `key_logn` | 1952.3 | 2227.2 | 2315.0 | 2529.1 |
| 9 | `key_frac` | 211.1 | 2565.6 | 568.8 | 2824.0 |
| 10 | `key_sqrt` | 572.0 | 2549.7 | 921.7 | 2825.9 |
| 11 | `key_pow2` | 923.7 | 2542.1 | 1238.4 | 2830.8 |
| 12 | `key_pown` | 1248.6 | 2549.5 | 1587.1 | 2836.7 |
| 13 | `key_log` | 1594.7 | 2548.6 | 1947.2 | 2829.8 |
| 14 | `key_ln` | 1940.8 | 2546.4 | 2302.2 | 2831.1 |
| 15 | `key_neg` | 212.3 | 2825.6 | 575.8 | 3098.9 |
| 16 | `key_deg` | 576.8 | 2824.7 | 921.6 | 3100.2 |
| 17 | `key_hyp` | 926.0 | 2831.9 | 1262.4 | 3099.6 |
| 18 | `key_sin` | 1259.0 | 2842.7 | 1598.8 | 3094.3 |
| 19 | `key_cos` | 1601.8 | 2836.2 | 1944.4 | 3088.1 |
| 20 | `key_tan` | 1945.4 | 2831.7 | 2302.9 | 3090.8 |
| 21 | `key_M_plus` | 1934.7 | 3096.1 | 2302.8 | 3398.5 |
| 22 | `key_SD` | 1600.1 | 3091.3 | 1934.3 | 3398.2 |
| 23 | `key_rparen` | 1265.9 | 3091.1 | 1598.5 | 3398.2 |
| 24 | `key_lparen` | 919.1 | 3097.5 | 1264.0 | 3397.4 |
| 25 | `key_eng` | 577.6 | 3103.5 | 920.8 | 3396.0 |
| 26 | `key_rcl` | 211.9 | 3099.6 | 574.6 | 3398.1 |
| 27 | `key_7` | 215.4 | 3401.4 | 657.0 | 3782.9 |
| 28 | `key_8` | 658.9 | 3399.2 | 1061.8 | 3779.4 |
| 29 | `key_9` | 1064.8 | 3401.8 | 1479.0 | 3773.7 |
| 30 | `key_del` | 1476.9 | 3403.1 | 1880.2 | 3771.4 |
| 31 | `key_ac` | 1878.8 | 3401.9 | 2314.8 | 3765.3 |
| 32 | `key_div` | 1884.2 | 3769.8 | 2309.4 | 4098.7 |
| 33 | `key_x` | 1482.1 | 3772.0 | 1880.9 | 4099.9 |
| 34 | `key_6` | 1064.9 | 3777.9 | 1488.4 | 4097.6 |
| 35 | `key_5` | 658.3 | 3782.5 | 1063.2 | 4096.6 |
| 36 | `key_4` | 226.0 | 3785.5 | 656.2 | 4105.8 |
| 37 | `key_1` | 235.0 | 4104.1 | 668.6 | 4430.7 |
| 38 | `key_2` | 660.4 | 4099.6 | 1073.6 | 4424.6 |
| 39 | `key_3` | 1071.4 | 4100.3 | 1475.3 | 4431.1 |
| 40 | `key_plus` | 1472.7 | 4103.7 | 1879.4 | 4431.2 |
| 41 | `key_minus` | 1881.6 | 4104.3 | 2303.3 | 4430.6 |
| 42 | `key_equals` | 1881.2 | 4435.2 | 2287.8 | 4761.9 |
| 43 | `key_Ans` | 1480.8 | 4434.6 | 1879.7 | 4766.2 |
| 44 | `key_pow10` | 1072.3 | 4432.6 | 1482.0 | 4768.6 |
| 45 | `key_comma` | 663.5 | 4429.2 | 1071.9 | 4767.1 |
| 46 | `key_0` | 243.4 | 4417.8 | 662.6 | 4771.3 |
| 47 | `key_dir3` | 991.2 | 1709.9 | 1862.8 | 2350.4 |
| 48 | `key_dir1` | 712.7 | 1981.0 | 1518.6 | 2624.3 |
| 49 | `key_dir2` | 1141.8 | 1842.4 | 1730.2 | 2650.9 |
| 50 | `key_dir0` | 471.0 | 1614.0 | 1247.0 | 2789.2 |

---

# 📎 Appendix G — গোল্ডেন টেস্ট তালিকা (`test_batch_1.json`, ১০০টা)

| # | টেস্টের নাম | কী কতটা | কী যাচাই করে |
|---|---|---|---|
| 1 | no input test | 0 | খালি/এরর |
| 2 | integer test | 10 | প্লেইন সংখ্যা |
| 3 | addition test | 10 | প্লেইন সংখ্যা |
| 4 | float test | 11 | প্লেইন সংখ্যা |
| 5 | fraction approximation test | 7 | ভগ্নাংশ |
| 6 | fraction to float test | 8 | প্লেইন সংখ্যা |
| 7 | fraction to float to fraction test | 9 | ভগ্নাংশ |
| 8 | float addition test | 16 | প্লেইন সংখ্যা |
| 9 | float subtraction test | 17 | প্লেইন সংখ্যা |
| 10 | float division test | 13 | ভগ্নাংশ |
| 11 | float multiplication test | 14 | প্লেইন সংখ্যা |
| 12 | integer fraction test | 9 | ভগ্নাংশ |
| 13 | float sqrt test | 8 | প্লেইন সংখ্যা |
| 14 | scientific result notation test | 17 | সাইন্টিফিক |
| 15 | pow test | 8 | সাইন্টিফিক |
| 16 | pow n test | 9 | প্লেইন সংখ্যা |
| 17 | log test | 7 | প্লেইন সংখ্যা |
| 18 | ln test | 7 | প্লেইন সংখ্যা |
| 19 | pow-1 test | 8 | ভগ্নাংশ |
| 20 | log n test | 12 | প্লেইন সংখ্যা |
| 21 | third root test | 6 | প্লেইন সংখ্যা |
| 22 | pow n test | 6 | প্লেইন সংখ্যা |
| 23 | sqrtn test | 11 | প্লেইন সংখ্যা |
| 24 | integer faculty test | 3 | প্লেইন সংখ্যা |
| 25 | epow test | 3 | প্লেইন সংখ্যা |
| 26 | pi test | 2 | প্লেইন সংখ্যা |
| 27 | e test | 2 | প্লেইন সংখ্যা |
| 28 | pow 10 integer test | 5 | প্লেইন সংখ্যা |
| 29 | ans test | 8 | প্লেইন সংখ্যা |
| 30 | auto ans test | 7 | প্লেইন সংখ্যা |
| 31 | order of basic operations test | 13 | প্লেইন সংখ্যা |
| 32 | brackets test | 11 | প্লেইন সংখ্যা |
| 33 | implicit multiplication test | 17 | প্লেইন সংখ্যা |
| 34 | sin cos tan test | 14 | প্লেইন সংখ্যা |
| 35 | user variables test | 24 | প্লেইন সংখ্যা |
| 36 | inline cursor movement test | 27 | প্লেইন সংখ্যা |
| 37 | fraction cursor movement test | 39 | ভগ্নাংশ |
| 38 | block inclusion in container operations | 31 | ভগ্নাংশ |
| 39 | ac test | 13 | প্লেইন সংখ্যা |
| 40 | del integer test | 17 | প্লেইন সংখ্যা |
| 41 | frac deletion | 10 | খালি/এরর |
| 42 | mixed test | 102 | প্লেইন সংখ্যা |
| 43 | different userLang test | 5 | প্লেইন সংখ্যা |
| 44 | DOES default to fraction test | 17 | ভগ্নাংশ |
| 45 | does NOT default to fraction test 3 | 18 | সাইন্টিফিক |
| 46 | asin test | 7 | প্লেইন সংখ্যা |
| 47 | acos test | 12 | প্লেইন সংখ্যা |
| 48 | atan test | 9 | প্লেইন সংখ্যা |
| 49 | Memory test | 29 | প্লেইন সংখ্যা |
| 50 | Throw error on Infinity test | 6 | খালি/এরর |
| 51 | CONST 1-5 test | 45 | প্লেইন সংখ্যা |
| 52 | CONST 6-10 test | 44 | প্লেইন সংখ্যা |
| 53 | rad to degree test | 22 | প্লেইন সংখ্যা |
| 54 | CONST 11-20 test | 74 | প্লেইন সংখ্যা |
| 55 | CONST abort test | 10 | প্লেইন সংখ্যা |
| 56 | CONST 21-30 test | 71 | প্লেইন সংখ্যা |
| 57 | CONST 31-40 test | 70 | প্লেইন সংখ্যা |
| 58 | (-) test | 10 | প্লেইন সংখ্যা |
| 59 | hyp test | 36 | প্লেইন সংখ্যা |
| 60 | toggle prefer_decimals test | 4 | প্লেইন সংখ্যা |
| 61 | cmplx test | 19 | ভগ্নাংশ |
| 62 | cmplx switch format test | 10 | প্লেইন সংখ্যা |
| 63 | trigonometry1 | 5 | প্লেইন সংখ্যা |
| 64 | trigonometry2 | 4 | প্লেইন সংখ্যা |
| 65 | trigonometry3 | 5 | প্লেইন সংখ্যা |
| 66 | trigonometry4 | 4 | প্লেইন সংখ্যা |
| 67 | trigonometry5 | 4 | প্লেইন সংখ্যা |
| 68 | trigonometry6 | 5 | প্লেইন সংখ্যা |
| 69 | trigonometry7 | 10 | প্লেইন সংখ্যা |
| 70 | negative third root test | 5 | প্লেইন সংখ্যা |
| 71 | basic integral test | 8 | ভগ্নাংশ |
| 72 | integral should result to fraction | 9 | ভগ্নাংশ |
| 73 | definition of i test | 4 | প্লেইন সংখ্যা |
| 74 | complex derivate test | 27 | প্লেইন সংখ্যা |
| 75 | Norm_1_rounding_test | 7 | সাইন্টিফিক |
| 76 | Norm_2_rounding_test | 7 | প্লেইন সংখ্যা |
| 77 | Fix_9 test | 12 | প্লেইন সংখ্যা |
| 78 | Fix_7 test | 12 | প্লেইন সংখ্যা |
| 79 | Fix_5 test | 12 | প্লেইন সংখ্যা |
| 80 | Fix_3 test | 12 | প্লেইন সংখ্যা |
| 81 | Fix_0 test | 12 | প্লেইন সংখ্যা |
| 82 | complex integral test | 29 | প্লেইন সংখ্যা |
| 83 | basic sexagesimal test | 25 | ভগ্নাংশ |
| 84 | mixed sexagesimal test | 12 | ভগ্নাংশ |
| 85 | angle_mode Deg test | 39 | প্লেইন সংখ্যা |
| 86 | angle_mode Rad test | 39 | প্লেইন সংখ্যা |
| 87 | angle_mode Gra test | 39 | প্লেইন সংখ্যা |
| 88 | sexagesimal output toggle test | 10 | প্লেইন সংখ্যা |
| 89 | ENG test | 6 | সাইন্টিফিক |
| 90 | eng-back test | 18 | সাইন্টিফিক |
| 91 | turn off and on test | 11 | প্লেইন সংখ্যা |
| 92 | turn off test | 11 | প্লেইন সংখ্যা |
| 93 | Frac Test 1 | 15 | ভগ্নাংশ |
| 94 | Frac Test 3 | 12 | ভগ্নাংশ |
| 95 | Frac Test 4 | 12 | ভগ্নাংশ |
| 96 | Frac Test 5 | 11 | ভগ্নাংশ |
| 97 | Frac Test 6 | 12 | ভগ্নাংশ |
| 98 | Frac Test 7 | 15 | ভগ্নাংশ |
| 99 | Frac Test 8 | 6 | ভগ্নাংশ |
| 100 | percent test | 11 | ভগ্নাংশ |

**+ `fraction_2.json`** → "Frac Test 2" (ভগ্নাংশ ÷ ভগ্নাংশ = `19/6`)
**+ `known_to_fail/`** → "does NOT default to fraction test 1" ও "… test 2" — এ দুটো `@Ignore`

---

# 📎 Appendix H — যেসব ফাঁদে সবাই পা দেয় (চেক করে নাও)

1. **`pop(2)` আসলে একটাই সরায়।** JS-এর `Array.pop()` আর্গুমেন্ট নেয় না।
   Kotlin-এ `removeLast()` — একবার।
2. **`ln` → `log(`, আর `log` → `log10(`।** math.js-এ `log` মানে প্রাকৃতিক লগ। উল্টে ফেললে
   ৪–৫টা টেস্ট চুপচাপ ভুল ফল দেবে।
3. **`[expr][1]` কেবল গ্রুপিং**, ম্যাট্রিক্স ফিচার নয়।
4. **হিট-টেস্টে bounding box চলবে না** — REPLAY প্যাডের চারটা তীর ওভারল্যাপ করে।
   পলিগন containment + উল্টো ডকুমেন্ট-অর্ডার।
5. **`Faculty_Element`-কে একটা বাড়তি আর্গুমেন্ট দেওয়া হয়** (`new Faculty_Element(cursor, false)`) —
   JS সেটা উপেক্ষা করে। Kotlin-এ প্যারামিটারই রেখো না।
6. **`nthRootComplex`-এ ফ্লোটিং-পয়েন্ট।** পোলার পদ্ধতিতে বাস্তব মূলেও `im ≈ 1e-16` থেকে যায় →
   `im == 0` চেক ফেল → ভুল ফল। §3.4.1-এর নিয়ম মানো।
7. **`sind(90)` ঠিক `1` হতে হবে** — `round(…, 15)` ছাড়া `0.9999999999999999` আসবে।
8. **`max_equations = 15` ডেড কোড** — লিমিট বসিও না।
9. **এরর হলে ইনপুট-এরিয়ায় `error`** লেখা দেখায়, আউটপুট খালি — উল্টোটা নয়।
10. **`Ans` = `results.last()`**, `results[displayIndex]` নয়।
11. **ভগ্নাংশের ফন্ট নেস্টেড হলেও ছোট হয় না** — CSS `rem` (রুট-সাপেক্ষ), `em` নয়।
    কিন্তু `.sqrt`-এর বর্ডার/প্যাডিং `em` (বর্তমান ফন্ট-সাপেক্ষ)। দুটো আলাদা!
12. **`key_dir3` (▲) দুই ধাপ সরে**: `cursor = cursor.up` তারপর `cursor = cursor.left`।
13. **কার্সরের প্রস্থ শূন্য** (`width: 0`) — সে জায়গা নেয় না, শুধু আঁকা হয়।
14. **প্রতি রেন্ডারে শেষে একটা NBSP (` `)** যোগ হয় — টেস্টে এটা না মিললে ফেল করবে।
15. **`°` রেগেক্সগুলোর ক্রম গুরুত্বপূর্ণ** — এলোমেলো করলে বৈধ ইনপুটও `error` হয়ে যাবে।
16. **`Intl.NumberFormat` লোকেল-নির্ভর।** `de-DE` → দশমিকে কমা। টেস্টে `userLang` দেখে নাও।
17. **`∫` ৫০,০০১ বার ইভ্যালুয়েট করে** — AST একবার পার্স করে বারবার চালাও, নইলে ANR।
18. **`turnOffClose` ডিফল্ট `false`** — মানে `SHIFT`+`AC` চাপলে অ্যাপ বন্ধ হয় না, "OFF" দেখায়।
19. **ইনপুট আর আউটপুট এরিয়া ওভারল্যাপ করে** — এটা বাগ নয়, ডিজাইন।
20. **`e` দুটো জিনিস**: `×10ˣ` কী-এর expr টুকরো (`5e3`) **এবং** অয়লার সংখ্যা।
    কিন্তু অয়লার সবসময় `(2.718281828459045)` আকারে বসে, তাই পার্সারে `e` কেবল
    সংখ্যার এক্সপোনেন্ট-মার্কার — আলাদা ধ্রুবক হিসেবে চেনার দরকার নেই।

---

# 📎 Appendix I — কাজ শেষে যে ফাইলগুলো থাকবে

```
MyCalculator/
├── AGENT_BUILD_INSTRUCTIONS.md      ← এই ফাইল
├── LICENSE                          ← GPL-3.0 ফুল টেক্সট
├── NOTICE.md                        ← অ্যাট্রিবিউশন (§3)
├── README.md
├── tools/generate_assets.py         ← Phase 2-এর জেনারেটর
├── app/src/main/
│   ├── AndroidManifest.xml          ← কোনো পারমিশন নেই
│   ├── java/com/my/calculator/…     ← §4.2-এর পুরো স্ট্রাকচার
│   └── res/
│       ├── drawable/calc_body.xml + keybg_*.xml + ind_*.xml
│       ├── font/schulrechner_regular.ttf, schulrechner_italic.ttf, schulrechner.xml
│       ├── raw/changelog.txt
│       └── values/{strings,colors,themes}.xml
├── app/src/test/
│   ├── java/…                       ← ইউনিট টেস্ট
│   └── resources/golden/*.json      ← ১০১টা গোল্ডেন কেস
└── app/src/androidTest/java/…       ← UI টেস্ট
```

---

# 🎯 শেষ কথা

- প্রতিটা ফেজ শেষ করে **কমিট** করো, কমিট মেসেজে ফেজ নম্বর দাও।
- কোথাও আটকে গেলে **থেমে যেও না** — ওই অংশটা `TODO(phase-N):` কমেন্ট দিয়ে চিহ্নিত করে
  বাকি সব শেষ করো, তারপর ফিরে এসো। শেষে একটা `KNOWN_ISSUES.md` বানিয়ে যা বাকি আছে লিখে দাও।
- **"মোটামুটি কাজ করছে" গ্রহণযোগ্য নয়।** ১০১টা গোল্ডেন টেস্ট পাস করা আর
  রেফারেন্স স্ক্রিনশটের সাথে মিল — এ দুটোই সাফল্যের মাপকাঠি।

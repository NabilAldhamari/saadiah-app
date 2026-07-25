# CLAUDE.md

Working agreement for this repository. Read this before making any change.

Saadiah is a free, offline-first Islamic companion app. It has no servers, no accounts, no analytics, no advertising, and no telemetry. Those are not marketing claims — they are architectural constraints, and several are enforced by CI.

---

## The five rules

1. **Test first.** Write the failing test, watch it fail, write the minimum code to pass, then refactor. Build files, resources and generated data are the only exceptions.
2. **Performance is the primary quality attribute.** Budgets are in `IMPLEMENTATION-PLAN.md` and enforced in CI. Do not regress one to make code prettier.
3. **Do not over-engineer.** No abstraction for a single implementation. No interface without two real callers. No framework where a function will do. When in doubt, write the simpler thing.
4. **Comments are a failure signal.** Name things so the code reads without them. A comment may explain *why* a non-obvious decision was made; it may never explain *what* the code does. Delete stale comments the moment you see one.
5. **Dependencies point inward.** `model` depends on nothing. Nothing depends on `app`. No cycles, ever.

---

## Commands

```bash
./gradlew check                 # format, static analysis, lint, tests, coverage gates
./gradlew allTests              # tests only
./gradlew koverHtmlReport       # coverage report
./gradlew :android:app:assembleRelease
scripts/check-apk-size.sh
scripts/check-forbidden-strings.sh
scripts/check-rtl.sh
```

`./gradlew check` must be green before every commit. Not before every PR — before every commit.

---

## Module map

```
core/model      value types, zero dependencies
core/prayer     prayer time computation, method registry, mosque solver
core/calendar   Hijri conversion, observance rules
core/schedule   pure alarm-spec computation, slot budgeting
core/content    Quran and adhkār access, Arabic-normalised search
core/data       settings, encrypted backup
design          Compose tokens, type scale, shared components
android/app     the application
```

Allowed dependencies:

| Module | May depend on |
|---|---|
| `model` | nothing |
| `prayer`, `calendar`, `content`, `data`, `design` | `model` |
| `schedule` | `model`, `prayer`, `calendar` |
| `app` | all of the above |

**Do not create Android feature modules.** One `:android:app` is correct until build times prove otherwise.

---

## Architecture decisions already made

Do not revisit these without opening an ADR in `docs/adr/`.

- **No dependency injection framework.** Manual constructor injection through one `AppContainer`. Annotation processors slow builds and buy nothing at this size.
- **No Room, no Retrofit, no Moshi.** SQLDelight for typed SQL. No HTTP client in the MVP.
- **No JSON parsing at runtime.** Bundled data is read-only SQLite or memory-mapped binary, generated offline by `tools/` and committed.
- **Prayer times are computed on device.** No network call is made to determine them, ever. The optional AlAdhan cross-check is the single exception and is off by default, user-initiated, and disclosed.
- **Android first.** iOS is Milestone 2. `core/*` is written once and does not change for it.
- **All bundled data is generated offline.** `tools/` scripts are never invoked by Gradle.

---

## Domain rules that are easy to get wrong

- **Maghrib is not always sunset.** Jaʿfarī and Tehran methods define it as a sun-depression angle. Never write `maghrib == sunset` anywhere, including in date logic.
- **The Hijri day begins at Maghrib, not midnight.** The 17th starts at sunset on the evening of the 16th. This is load-bearing for hijama windows and fasting alerts.
- **`Tradition` scopes content, not just calculation.** Every observance rule and every adhkār entry is tagged with the traditions it applies to and filtered at query time. Showing a Sunni-framed fasting recommendation to a Twelver user is a content failure, not a cosmetic one. ʿĀshūrāʾ is the case that catches people.
- **Normalise Arabic before indexing and before querying.** Strip diacritics, unify alif forms, tāʾ marbūṭa → hāʾ, alif maqṣūra → yāʾ, remove tatwīl. Never search the Uthmani form directly.
- **Prayer alerts are correctness-critical.** A missed or late alert on a physical device is a P0 bug, not a polish item.

---

## Accessibility — non-negotiable

The primary users include people in their seventies. These are hard requirements, not aspirations.

- Body text 17sp minimum. Secondary 15sp minimum. **Nothing below 14sp anywhere.**
- Honour the system font scale up to 200%. Layouts reflow; they never clip.
- Every foreground/background token pair meets WCAG AA. There is a unit test for this — if it fails, the colour is wrong, not the test.
- Tap targets 48dp minimum with 8dp separation.
- Every interactive control in a primary flow has a visible text label. Icon-only affordances are not permitted there.
- No information is encoded in colour alone. Every marker carries a word or a shape too.
- Durations are spelled out: "in 2 hours 14 minutes", never "2h 14m".
- Never name a feature after the user's age. "Clear", not "Senior mode".

## RTL — non-negotiable

- `start`/`end` only. Never `left`/`right`. `scripts/check-rtl.sh` fails the build on violations.
- Every screen has a Roborazzi screenshot test in LTR and RTL, at font scale 1.0 and 2.0.
- Wrap mixed-direction runs in bidi isolates. Times inside Arabic sentences are where this breaks first.
- Arabic is the primary locale. Write religious copy in Arabic first and translate to English, not the reverse.

---

## Privacy constraints enforced by CI

- No dependency may introduce network access. `scripts/check-forbidden-strings.sh` scans the release binary for hostnames not in `config/allowed-hosts.txt`.
- No analytics, crash-reporting, advertising or attribution SDK. Ever. Crash handling writes a redacted local file the user chooses to send.
- No permission is added without a corresponding entry in the README's permission table and a graceful degradation path when it is denied.
- `SYSTEM_ALERT_WINDOW` and background location are permanently forbidden.

---

## Testing

| Kind | Where | Tool |
|---|---|---|
| Unit | all `core/*` | `kotlin.test`, JVM |
| Fixture-driven | `prayer`, `calendar` | JSON fixtures in `commonTest/resources` |
| Screenshot | `design`, `app` | Roborazzi, JVM |
| Robolectric | `app` scheduling | Robolectric |
| Instrumented | alarm delivery only | Gradle Managed Device |
| Benchmark | performance budgets | JVM benchmark + Macrobenchmark |

Coverage gates: `core/*` at 90% line and 85% branch. UI glue is excluded deliberately — do not add tests that exist only to move the number. If a branch is hard to test, that usually means the design is wrong; fix the design.

For randomised invariants use a seeded loop. Do not add a property-testing library for this.

---

## Commits and releases

Conventional Commits, enforced by CI. Scope is the module: `feat(prayer):`, `fix(schedule):`, `perf(content):`.

`release-please` derives the version and changelog from commit history. `version.txt` is the single source of version truth; `versionCode` is derived from it. Never edit a version by hand.

Squash merge only. The PR title becomes the commit, so it must be conventional.

---

## When to stop and ask

Stop and raise it rather than working around it if:

- A task cannot be done without violating a ground rule.
- A performance budget cannot be met without adding complexity.
- A dependency would introduce network access, an annotation processor, or a runtime reflection cost.
- A religious-content question has more than one defensible answer. Expose it as a setting and stay neutral; do not pick a position in code.

# Saadiah — Implementation Plan

An ordered, task-by-task build plan intended to be executed by Claude Code. Every task has explicit tests-first steps, acceptance criteria, and a commit message. Work strictly in order; each phase leaves the repository green.

**Scope of this plan: Android + shared Kotlin core.** iOS is Milestone 2 and is described at the end.

---

## Ground rules

These are non-negotiable and are restated in `CLAUDE.md`.

1. **TDD, always.** Red → green → refactor. No production code is written before a failing test that requires it. The only exceptions are build files, resources, and generated data.
2. **Performance is the primary quality attribute.** Every budget in §Performance is enforced by CI. A PR that regresses a budget does not merge.
3. **No over-engineering.** No abstraction is introduced for a single implementation. No interface without two callers or a test double that earns its place. No framework where a function will do.
4. **Comments are a failure signal.** Name things so the code reads without them. Comment only to explain *why* a non-obvious decision was made — never *what* the code does. Delete stale comments on sight.
5. **SOLID, applied with judgement.** Dependency direction flows inward toward `:core:model`. No cycles. Each module has one reason to change.
6. **Every change is a conventional commit.** `release-please` derives the changelog and version from them.

---

## Repository layout

```
saadiah/
├─ .github/
│  ├─ workflows/{ci,release-please,release-android,benchmark}.yml
│  ├─ pull_request_template.md
│  └─ renovate.json5
├─ build-logic/                  convention plugins — one per module archetype
├─ gradle/libs.versions.toml     single source of dependency truth
├─ core/
│  ├─ model/                     value types only, zero dependencies
│  ├─ prayer/                    prayer time computation, methods, tuning, solver
│  ├─ calendar/                  Hijri conversion + observance rules
│  ├─ schedule/                  pure alarm-spec computation
│  ├─ content/                   Quran + adhkār access, search, normalisation
│  └─ data/                      settings, backup, persistence
├─ design/                       Compose design system: tokens, type scale, components
├─ android/app/                  the application
├─ tools/                        offline data pipeline scripts (not built by Gradle)
├─ docs/                         ADRs, data provenance, licences of bundled content
├─ version.txt                   the single source of version truth
├─ CLAUDE.md
├─ README.md
├─ COPYING                       AGPL-3.0 + app store exception
└─ .gitignore .editorconfig .gitattributes
```

**Module dependency rule, enforced by review and by a Gradle check:**

```
model   ← (nothing)
prayer  ← model
calendar← model
schedule← model, prayer, calendar
content ← model
data    ← model
design  ← model
app     ← all
```

**Do not create Android feature modules yet.** A single `:android:app` is correct until build times demand a split. Splitting early is ceremony, not architecture.

---

## Performance budgets — CI-enforced

| Budget | Limit | Enforced by |
|---|---|---|
| Cold start, P50, mid-range device | ≤ 400 ms | Macrobenchmark job |
| Today screen full state recompute | ≤ 3 ms | JVM benchmark test |
| One day of prayer times | ≤ 1 ms | JVM benchmark test |
| Quran FTS query over 6,236 āyāt, P95 | ≤ 25 ms | JVM benchmark test |
| City prefix search per keystroke | ≤ 8 ms | JVM benchmark test |
| Release APK, arm64 split | ≤ 12 MB | `scripts/check-apk-size.sh` |
| Base install download size | ≤ 10 MB | Same |

Non-negotiable performance decisions, decided once so they aren't re-litigated per PR:

- R8 full mode, resource shrinking, ABI splits, `android:extractNativeLibs="false"`.
- Baseline Profile generated and committed; CI fails if it is missing from the release artefact.
- **No annotation processors.** No Hilt, no Room, no Moshi codegen. Manual constructor injection through a single `AppContainer`. SQLDelight for typed SQL (compile-time, no runtime reflection).
- **No HTTP client dependency in the MVP.** Content packs are the only network feature and land in Milestone 2; when they do, a thin `HttpURLConnection` wrapper is sufficient.
- Compose: all UI state classes `@Immutable`, no lambdas allocated in composition, `derivedStateOf` for the countdown only.
- All bundled data is read-only SQLite or memory-mapped binary. No JSON parsing at runtime.

---

## Phase 0 — Foundation

Leaves a repository with zero production code that is fully green, releasable, and self-updating. Do not start Phase 1 until every acceptance criterion here passes.

### T0.1 — Repository skeleton

- `git init`, default branch `main`.
- `.gitignore` covering Gradle, Android, IntelliJ, macOS, Xcode, and `local.properties`.
- `.editorconfig` — 4-space Kotlin indent, LF, final newline, max line 120.
- `.gitattributes` — force LF; mark `core/content/src/commonMain/resources/**` as binary and `linguist-generated`.
- `COPYING` — AGPL-3.0 text plus this additional permission verbatim:

> As an additional permission under section 7, you are allowed to distribute this software through an app store, even if that store has restrictive terms and conditions that are incompatible with the AGPL, provided that the source is also available under the AGPL, with or without this permission, through a channel without those restrictive terms and conditions.

  Without this clause the app cannot legally ship on the App Store — this is the reason GNU Go and VLC were removed. Add it now, while you are the sole copyright holder and it costs nothing.

**Acceptance:** repo initialised, `COPYING` present with the exception, nothing else builds yet.

**Commit:** `chore: initialise repository`

### T0.2 — Gradle foundation

- Gradle wrapper (latest stable), `settings.gradle.kts` with all modules declared but empty.
- `gradle/libs.versions.toml` as the only place versions appear. Renovate reads this file natively.
- `build-logic/` included build with three convention plugins: `saadiah.kotlin.multiplatform`, `saadiah.android.library`, `saadiah.android.application`. Each configures Kotlin, JVM target, test framework, Kover, and Detekt once.
- `version.txt` containing `0.1.0`. Root `build.gradle.kts` reads it; `versionCode` is derived as `major * 10000 + minor * 100 + patch`.

**Acceptance:** `./gradlew build` succeeds on an empty project. No version string appears anywhere except `version.txt` and `libs.versions.toml`.

**Commit:** `build: add gradle foundation and convention plugins`

### T0.3 — Static analysis

- Spotless with ktlint, applied to all Kotlin and the Gradle scripts.
- Detekt with a committed `config/detekt.yml`. Explicitly **disable** `UndocumentedPublicClass`, `UndocumentedPublicFunction`, `UndocumentedPublicProperty` — this codebase does not document what code does. Explicitly **enable** `LongMethod` (30), `LongParameterList` (5), `CyclomaticComplexMethod` (10), `TooManyFunctions`, `MagicNumber` (with `core/prayer` astronomy constants excluded by suppression at the declaration site).
- Android Lint with `abortOnError true` and `warningsAsErrors true`.
- Wire all of it into `./gradlew check`.

**Acceptance:** `./gradlew check` passes and fails correctly when a deliberate violation is introduced.

**Commit:** `build: add spotless, detekt and lint`

### T0.4 — Guard scripts

Three small shell scripts in `scripts/`, each callable locally and from CI:

- `check-apk-size.sh` — fails if the arm64 release APK exceeds the budget.
- `check-forbidden-strings.sh` — extracts strings from the release APK and fails on any hostname not in `config/allowed-hosts.txt`. This is what makes the privacy claim mechanically true rather than aspirational.
- `check-rtl.sh` — greps Kotlin sources for `padding(start` mixed with absolute offsets, and for `Alignment.CenterStart` misuse; fails on any occurrence of `.padding(left =` or `Arrangement.Absolute`. Cheap, and it prevents the single most expensive class of bilingual bug.

**Acceptance:** each script exits non-zero on a seeded violation and zero otherwise.

**Commit:** `build: add apk size, forbidden string and rtl guard scripts`

### T0.5 — CI pipeline

`.github/workflows/ci.yml`:

```yaml
name: ci
on:
  pull_request:
  push:
    branches: [main]
concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true
jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '21' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew spotlessCheck detekt lint
      - run: ./gradlew allTests koverVerify
      - run: ./gradlew :android:app:assembleRelease
      - run: scripts/check-apk-size.sh
      - run: scripts/check-forbidden-strings.sh
      - run: scripts/check-rtl.sh
      - uses: actions/upload-artifact@v4
        if: failure()
        with: { name: test-reports, path: '**/build/reports/**' }
  commitlint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 0 }
      - uses: wagoid/commitlint-github-action@v6
```

Branch protection on `main`: require `verify` and `commitlint`, require PR, squash merge only, linear history.

**Acceptance:** CI green on an empty project; a PR with a non-conventional title fails.

**Commit:** `ci: add verification pipeline`

### T0.6 — release-please

`release-please-config.json`:

```json
{
  "packages": {
    ".": {
      "release-type": "simple",
      "package-name": "saadiah",
      "changelog-sections": [
        { "type": "feat", "section": "Features" },
        { "type": "fix", "section": "Fixes" },
        { "type": "perf", "section": "Performance" },
        { "type": "revert", "section": "Reverts" },
        { "type": "docs", "section": "Documentation", "hidden": true },
        { "type": "build", "section": "Build", "hidden": true },
        { "type": "ci", "section": "CI", "hidden": true },
        { "type": "chore", "section": "Chores", "hidden": true },
        { "type": "test", "section": "Tests", "hidden": true },
        { "type": "refactor", "section": "Refactoring", "hidden": true }
      ]
    }
  },
  "$schema": "https://raw.githubusercontent.com/googleapis/release-please/main/schemas/config.json"
}
```

`.release-please-manifest.json`: `{ ".": "0.1.0" }`

`.github/workflows/release-please.yml` runs `googleapis/release-please-action@v4` on push to `main`, in manifest mode. It maintains `version.txt` and `CHANGELOG.md` and opens a release PR. Merging that PR creates the tag.

**Acceptance:** a `feat:` commit on `main` opens a release PR proposing `0.2.0`; merging it tags `v0.2.0`.

**Commit:** `ci: add release-please`

### T0.7 — Renovate

`.github/renovate.json5`:

```json5
{
  $schema: "https://docs.renovatebot.com/renovate-schema.json",
  extends: ["config:recommended", ":semanticCommits"],
  schedule: ["before 6am on monday"],
  prConcurrentLimit: 3,
  labels: ["dependencies"],
  lockFileMaintenance: { enabled: true, schedule: ["before 6am on monday"] },
  packageRules: [
    { matchManagers: ["gradle"], matchPackagePatterns: ["^org.jetbrains.kotlin"], groupName: "kotlin" },
    { matchManagers: ["gradle"], matchPackagePatterns: ["^androidx.compose"], groupName: "compose" },
    { matchManagers: ["gradle"], matchPackagePatterns: ["^com.android.tools"], groupName: "agp" },
    { matchDepTypes: ["devDependencies"], matchUpdateTypes: ["minor", "patch"], automerge: true },
    { matchUpdateTypes: ["major"], dependencyDashboardApproval: true },
  ],
}
```

Enable the Mend Renovate GitHub App on the repository — free for public repos.

**Acceptance:** Renovate opens its onboarding PR and the dependency dashboard issue.

**Commit:** `ci: add renovate configuration`

### T0.8 — Documentation

- `README.md` — what the app is, the privacy guarantees stated as verifiable facts, build instructions, how to reproduce a release build and compare hashes, the licence and the app store exception, and content provenance with attributions for Tanzil, KFGQPC and GeoNames.
- `CLAUDE.md` — the working agreement (supplied separately; drop it in as-is).
- `docs/adr/0001-android-first.md` and `docs/adr/0002-no-di-framework.md` — two paragraphs each. Record decisions where a future reader would otherwise ask "why not X?". Do not write ADRs for anything else yet.

**Acceptance:** a stranger can clone, build, and verify a release hash from the README alone.

**Commit:** `docs: add readme, claude.md and initial adrs`

---

## Phase 1 — `:core:model` and `:core:prayer`

### T1.1 — Value types

Types only, no behaviour beyond validation: `Coordinates`, `CityId`, `City`, `Prayer` (enum), `DayTimings`, `HijriDate`, `Tradition`, `Madhab`, `MaghribMode`, `MidnightMode`, `CombineMode`, `HighLatitudeRule`, `TimingProfile`, `AlarmSpec`, `ObservanceKind`.

**Tests first:** invalid latitude rejected, invalid Hijri day rejected, `TimingProfile` equality and copy semantics.

**Acceptance:** module has zero dependencies beyond `kotlinx-datetime`. Kover ≥ 90%.

**Commit:** `feat(model): add core value types`

### T1.2 — Prayer time computation

Wrap `com.batoulapps.adhan:adhan2` behind `PrayerCalculator`. Single public function: `fun compute(city: City, date: LocalDate, profile: TimingProfile): DayTimings`.

**Tests first:** a data-driven fixture runner reading `core/prayer/src/commonTest/resources/fixtures/*.json`. Seed it from the upstream adhan test data plus ~40 hand-chosen cities spanning Tromsø to Ushuaia, every method, both madhāhib, and both sides of each DST transition. Tolerance: exact to the minute.

**Then:** add the Jaʿfarī preset (Fajr 16°, Maghrib angle 4°, Isha 14°, Jaʿfarī midnight) — the upstream library does not ship it. Verify `maghribAngle` is honoured in the Kotlin port; if it is not, implement Maghrib-by-angle in this module and test it independently.

**Acceptance:** all fixtures pass. `DayTimings` never assumes `maghrib == sunset`. Benchmark: one day ≤ 1 ms.

**Commit:** `feat(prayer): add prayer time computation with sunni and shia methods`

### T1.3 — Method inference

`fun inferProfile(country: CountryCode): TimingProfile` — a table, not a heuristic. Umm al-Qura for SA, Karachi + Hanafi for PK/IN/BD, Diyanet for TR, MWL for Europe, ISNA for NA, Egyptian for EG, Tehran for IR.

**Tests first:** every entry in the table asserted; unknown country falls back to MWL plus `HighLatitudeRule.recommended()`.

**Acceptance:** table is exhaustive over the countries present in the city database.

**Commit:** `feat(prayer): infer calculation method from country`

### T1.4 — Match-my-mosque solver

`fun solve(observed: Map<Prayer, LocalTime>, city: City, date: LocalDate): SolveResult` — ranks every method × madhhab combination by total absolute error, returns the best plus residual per-prayer tuning.

**Tests first:** feed it the output of a known profile and assert it recovers that profile exactly; feed it a profile plus a uniform +3 minute offset and assert it recovers the profile and the tuning; feed it contradictory input and assert it returns a low-confidence result rather than a wrong confident one.

**Acceptance:** 100% branch coverage on this function. It is the feature that removes the loudest review complaint, so it gets the strictest gate.

**Commit:** `feat(prayer): add mosque timetable solver`

---

## Phase 2 — `:core:calendar`

### T2.1 — Hijri conversion

Umm al-Qura month-length table for 1356–1500 AH as a packed bit array in a Kotlin source file generated by `tools/gen-hijri-table.kt` and committed. Tabular Kuwaiti algorithm as the out-of-range fallback.

**Tests first:** fixtures asserting conversion both directions for at least ten years of published Saudi calendar dates, plus the boundary years of the table, plus the fallback path.

**Acceptance:** table under 3 KB in the binary. Conversion allocation-free.

**Commit:** `feat(calendar): add umm al-qura hijri conversion`

### T2.2 — Maghrib day boundary

The Hijri day runs `[maghrib(d), maghrib(d+1))`. This depends on `:core:prayer`, so `HijriDate` resolution takes a `DayTimings`.

**Tests first:** an instant one minute before Maghrib resolves to the earlier Hijri day; one minute after, the later one. Test with a Jaʿfarī profile where Maghrib is not sunset — this is the case that silently breaks.

**Commit:** `feat(calendar): resolve hijri day at maghrib boundary`

### T2.3 — Observance rules

Data-driven. Each rule is `ObservanceRule(kind, traditions, predicate)`. Sunni and Twelver sets defined separately; queries filter on the user's tradition.

**Tests first:** a fixture asserting every observance for a full 12-month Hijri cycle per tradition, including: Ayyām al-Bīḍ suppressed on Tashrīq days, Eid days marked as fasting-prohibited, ʿArafah for non-pilgrims, the six of Shawwāl, hijama on 17/19/21, and the Muḥarram divergence between traditions.

**Acceptance:** no rule is untagged. A test asserts that the union of traditions on every rule is non-empty and that no rule leaks across traditions.

**Commit:** `feat(calendar): add fasting and hijama observance rules`

---

## Phase 3 — `:core:schedule`

### T3.1 — Alarm specification

`fun schedule(settings: AlertSettings, from: Instant, horizon: Duration): List<AlarmSpec>` — pure, deterministic, no platform types.

**Tests first:**
- Never returns a spec in the past.
- Respects per-prayer enable flags, pre-alerts and end-of-window alerts.
- Collapses Ẓuhrayn and ʿIshāʾayn into single specs when `CombineMode.ZUHRAYN_ISHAAYN`.
- Deterministic: same inputs, same output, byte-identical.
- Correct across a DST transition and a timezone change.

### T3.2 — Budgeted scheduling

`fun budget(specs: List<AlarmSpec>, max: Int): List<AlarmSpec>` — the iOS 64-slot allocator, written now because it is pure and testable on the JVM even though it ships in Milestone 2.

**Tests first:** never exceeds `max`; prefers nearer specs; never drops a spec while a later one survives; reserves a slot for the re-arm reminder when the horizon would otherwise lapse.

**Acceptance:** deterministic seeded random inputs, 10,000 iterations, invariants hold. Hand-rolled loop with a fixed seed — do not add a property-testing dependency for this.

**Commit:** `feat(schedule): add pure alarm scheduling with slot budgeting`

---

## Phase 4 — `:core:data`

### T4.1 — Settings

A single `SettingsRepository` over `androidx.datastore` behind an `expect`/`actual` boundary. One flow of one immutable `Settings` object. No per-key API — that invites partial writes.

**Tests first:** defaults are correct on first read; a write is observable; a corrupt file falls back to defaults without throwing.

### T4.2 — Backup

`fun export(state: UserState, passphrase: CharArray): ByteArray` and `import`. Argon2id key derivation, XChaCha20-Poly1305.

**Tests first:** round-trip equality; wrong passphrase throws a typed error and never returns partial data; a truncated archive is rejected; the format carries a version byte and an unknown version is rejected with a clear error.

**Acceptance:** no user state is reachable without the passphrase. Round-trip of 1,000 bookmarks ≤ 200 ms.

**Commit:** `feat(data): add settings store and encrypted backup`

---

## Phase 5 — content pipeline and `:core:content`

### T5.1 — Offline data pipeline

Scripts in `tools/`, run manually, output committed as binary assets with SHA-256 manifests in `docs/data-provenance.md`. **They do not run at build time** — build reproducibility and build speed both depend on that.

- `gen-geo.kt` — trim GeoNames `cities5000` to `name, ascii, country, admin1, lat, lng, tz`, emit a prefix-indexed binary blob.
- `gen-quran-db.kt` — Tanzil Uthmani text into SQLite with an FTS5 index over the normalised form, plus a per-āyah SHA-256 column.
- `gen-adhkar-db.kt` — the adhkār corpus with source references, repetition counts, and tradition tags.

### T5.2 — Arabic normalisation

`fun normalise(text: String): String` — strip diacritics U+064B–U+0652 and U+0670, unify alif forms (أ إ آ ٱ → ا), tāʾ marbūṭa → hāʾ, alif maqṣūra → yāʾ, remove tatwīl.

**Tests first:** a table of ~40 input/expected pairs covering each rule and their interactions. This function determines whether search feels broken, so it is tested to the character.

### T5.3 — Content access

Read-only SQLDelight queries. Reading threads, bookmarks, notes.

**Tests first:** a search for a term written with different hamza forms returns the same results; an āyah checksum mismatch throws rather than rendering; thread positions are independent.

**Acceptance:** FTS query P95 ≤ 25 ms. Checksum verification of the full text ≤ 300 ms and runs once on first launch, off the main thread.

**Commit:** `feat(content): add quran and adhkar access with arabic-normalised search`

---

## Phase 6 — `:design`

### T6.1 — Tokens and type scale

The accessible defaults, not the original sketch values: body 17sp, secondary 15sp, floor 14sp, Quran base 26sp. Light-mode accent `#7A5A22`, dark-mode accent `#B8935A`. Tap target minimum 48dp as a `Modifier.minimumTouchTarget()`.

**Tests first — this one is unusual and worth the effort:** a unit test that iterates every declared foreground/background token pair in both themes and asserts a WCAG contrast ratio ≥ 4.5:1 for text and ≥ 3:1 for non-text. It is roughly twenty lines and it makes the accessibility commitment impossible to regress silently.

### T6.2 — Components

`PrayerRow`, `ObservanceRow`, `NextPrayerHero`, `Counter`, `LabelledIconButton`, `SectionDivider`. Every interactive component takes a text label — icon-only affordances are not permitted in primary flows.

**Tests:** Roborazzi screenshot tests for each component across the matrix LTR/RTL × light/dark × font scale 1.0/2.0. Runs on the JVM under Robolectric, so no emulator is needed in CI.

**Acceptance:** contrast test green; no screenshot clips at font scale 2.0.

**Commit:** `feat(design): add tokens, accessible type scale and base components`

---

## Phase 7 — `:android:app`

### T7.1 — Application shell

`AppContainer` constructing every dependency by hand. Single activity, Compose navigation with four destinations. No DI framework.

### T7.2 — Today screen

**Tests first:** ViewModel tests over a fake clock asserting next-prayer selection at each boundary, countdown formatting spelled out in full, and the observance list ordering.

### T7.3 — Alarm scheduling

`AlarmManager.setAlarmClock()`, a `BootReceiver`, and receivers for `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED`. Rolling 3-day horizon, re-armed on every fire.

**Tests first:** Robolectric tests asserting that a scheduled alarm is registered, that a simulated boot re-registers the full horizon, that a timezone change reschedules, and that firing an alarm arms the next one. Declare `USE_EXACT_ALARM`, and test the `SCHEDULE_EXACT_ALARM` fallback path explicitly.

**Integration test:** one instrumented test on a Gradle Managed Device that schedules an alarm 5 seconds out and asserts delivery. This is the one place an emulator is genuinely required.

### T7.4 — Notification Doctor

OEM detection, deep links to the manufacturer settings pages, and a local delivery log.

**Tests first:** each `Build.MANUFACTURER` value maps to the correct intent; an unresolvable intent degrades to generic battery settings rather than crashing; the delivery log correctly classifies on-time versus late.

### T7.5 — Remaining screens

Calendar (list view is the default, grid is the alternative), Quran reader with threads and notes, adhkār with a persisted counter, settings including the Privacy Receipt and Offline Lock.

### T7.6 — Widgets

Glance widget with the next-prayer countdown.

**Acceptance for the phase:** macrobenchmark cold start ≤ 400 ms P50 with the baseline profile applied; all guard scripts green.

**Commits:** one per task, `feat(android): …`

---

## Phase 8 — Release

### T8.1 — Signing and release workflow

`.github/workflows/release-android.yml`, triggered on tags matching `v*`:

1. Decode the keystore from `secrets.KEYSTORE_BASE64`.
2. `./gradlew bundleRelease assembleRelease`.
3. Generate and verify the baseline profile is embedded.
4. Compute SHA-256 of every artefact and write `SHA256SUMS`.
5. Attach APK, AAB and `SHA256SUMS` to the GitHub Release created by release-please.
6. Optional: upload the AAB to the Play internal track via `r0adkll/upload-google-play`.

### T8.2 — Reproducible build verification

A second job that rebuilds from a clean checkout at the tag in a pinned container and asserts the APK hash matches. Fail the release if it does not. This job is the technical substance behind the entire trust position — without it the claim is marketing.

### T8.3 — F-Droid

Metadata in `fastlane/metadata/android/`, plus an RFP to F-Droid once the first tag is cut.

**Commit:** `ci: add signed release and reproducible build verification`

---

## Milestone 2 — iOS

Only after Milestone 1 is on the Play Store and stable. Nothing in `:core:*` changes; the work is a SwiftUI application plus:

- `UNUserNotificationCenter` scheduling driven by `:core:schedule`, using the already-tested 64-slot budgeter.
- `interruptionLevel = .timeSensitive`.
- A trimmed adhan under 30 seconds in CAF, and honest copy in Settings explaining why the full adhan cannot play from a notification.
- `BGAppRefreshTask` re-arm, WidgetKit, Live Activity.
- A `macos-latest` CI job. Free minutes on public repositories, but slower — keep it on a separate workflow so Android PRs are not blocked by it.

---

## What "done" means

A phase is done when: every task's tests pass, `./gradlew check` is green, coverage gates hold, every performance budget is met, all three guard scripts pass, and the branch merges to `main` with a conventional commit that release-please can read.

If a task cannot be completed without violating a ground rule — particularly the no-over-engineering rule — stop and raise it rather than working around it.

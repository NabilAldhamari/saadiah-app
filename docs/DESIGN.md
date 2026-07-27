# Saadiah — Design Specification

Authoritative. Where this document and a mockup disagree, this document wins. Where this document
and an implementation disagree, this document wins.

Reference mockups: `docs/mockups/mockups.html`. Open it in a browser. It renders every screen at
the values below. It is a picture of the intended result, not a source of measurements — take
measurements from this file.

**Append to `CLAUDE.md` under a `## Design` heading:**

> `docs/DESIGN.md` is the authoritative design specification. Do not invent colours, type sizes,
> spacing or component APIs. If a value you need is not in it, stop and ask rather than choosing
> one. Every token pair is contrast-verified; changing a colour requires re-running
> `ColorContrastTest` and updating the ratio recorded in `DESIGN.md`.

---

## 1. Principles

1. **Subtractive.** Every element must justify its presence. When in doubt, remove it.
2. **Nothing is promotional.** No banners, no cards competing for attention, no streaks, no
   celebration animations, no engagement mechanics of any kind.
3. **Legibility beats density.** Comfortable is the default; compact is the opt-in.
4. **One idea per screen.** The Today screen has exactly one hero: the next prayer.

---

## 2. Colour tokens

**Corrected to match what ships.** The hex values below were read back out of `Tokens.kt` and
every ratio re-measured with the WCAG 2.1 formula; the numbers in the tables are the measured
ones, not the originally proposed ones. `ColorContrastTest` holds each to its gate and also
asserts the recorded ratio matches the measurement, so this section and the code cannot drift
apart silently.

A third palette, **white and green**, was added later and is recorded further down. Every token
in all three passes.

**What does not change:** `ColorContrastTest` still gates the build regardless of which palette
it's checking. ≥ 4.5:1 for text roles, ≥ 3:1 for non-text roles, in both themes. If the shipped
palette hasn't been run through it yet, run it now — the accent-on-light failure recorded below
happened once already precisely because a colour was chosen before it was measured.

Two themes. Follow the system setting — do not pick for the user, and do not default to dark.

### Dark

| Token | Hex | Use | Contrast on `bg` |
| --- | --- | --- | --- |
| `bg` | `#0E1614` | screen background | — |
| `surface` | `#152220` | cards, inset panels | 1.4:1 (non-text) |
| `line` | `#22302C` | dividers, hairlines | decorative |
| `lineSubtle` | `#1A2724` | ayah separators | decorative |
| `text` | `#F7F4EC` | primary text | 16.71:1 |
| `textSecondary` | `#B0BAB6` | translations, supporting copy | 9.22:1 |
| `textTertiary` | `#78837F` | labels, metadata | 4.68:1 |
| `accent` | `#B8935A` | active state, primary action | 6.44:1 |
| `sage` | `#5E7A6B` | recommended-fast marker | 3.91:1 — **markers only, never text** |
| `warning` | `#C57358` | failed check, prohibited day | 5.22:1 |

### Light

| Token | Hex | Use | Contrast on `bg` |
| --- | --- | --- | --- |
| `bg` | `#F7F4EC` | screen background | — |
| `surface` | `#EDE6D3` | cards, inset panels | 1.2:1 (non-text) |
| `line` | `#DDD7C7` | dividers | decorative |
| `lineSubtle` | `#EAE5D8` | row separators | decorative |
| `text` | `#0E1614` | primary text | 16.71:1 |
| `textSecondary` | `#4A4A44` | supporting copy | 8.12:1 |
| `textTertiary` | `#6B665C` | labels, metadata | 5.19:1 |
| `accent` | `#7A5A22` | active state, primary action | 5.77:1 |
| `sage` | `#3E5A4A` | recommended-fast marker | 6.91:1 |
| `warning` | `#9A3F22` | failed check, prohibited day | 6.15:1 |

**Do not use `#B8935A` on a light background.** It measures 2.60:1 and fails. That is what
`accent` light exists for. This mistake was made once already.

`ColorContrastTest` iterates every foreground/background pair declared in `Tokens.kt` and asserts
≥ 4.5:1 for text roles and ≥ 3:1 for non-text roles. `sage` in dark is registered as a non-text
role; using it for text is a test failure, not a judgement call.

---

## 3. Type scale

| Token | Size | Line height | Weight | Use |
| --- | --- | --- | --- | --- |
| `display` | 54sp | 1.1 | 400 | the next prayer time, and nothing else |
| `titleLarge` | 24sp | 1.3 | 400 | prayer name in the hero, sheet titles |
| `titleMedium` | 21sp | 1.3 | 400 | screen headers, Hijri date |
| `titleSmall` | 19sp | 1.4 | 400 | prayer rows, list primary text |
| `body` | 17sp | 1.5 | 400 | default body — **the floor for anything a user reads** |
| `bodySmall` | 15sp | 1.5 | 400 | supporting lines under a primary line |
| `label` | 14sp | 1.4 | 500 | tab labels, chips, metadata. **Absolute minimum size.** |
| `quran` | 26sp | 2.0 | 400 | Quranic text, base value |
| `arabicUi` | inherits | 1.6 | 400 | Arabic UI text — line height differs from Latin |

Two weights only: 400 and 500. No 300, no 600, no italics except transliteration.

**Nothing below 14sp exists anywhere in this app.** Not metadata, not captions, not legends.

**Scaling.** Honour the system font scale to 200%. Every layout reflows; nothing clips, nothing
truncates with an ellipsis, nothing becomes horizontally scrollable. `quran` scales to 44sp.
Screenshot tests run at scale 1.0 and 2.0 and both must pass.

**Fonts.** UI Latin: one geometric-humanist family. UI Arabic: a clean naskh. Quran: KFGQPC Hafs
Uthmanic. Line height is set per script — a single global line height clips Arabic diacritics.

**Numerals** are a user setting (Western `0123` or Eastern Arabic `٠١٢٣`), never inferred from
the locale.

---

## 4. Spacing, shape, motion

Spacing scale, in dp: `4, 8, 12, 16, 20, 24, 32`. Nothing else. Screen horizontal padding is 20.

Radii: `8` buttons and cards, `12` sheets and inset panels, `20` full-bleed containers,
`999` pills and chips.

Touch targets: **48dp minimum**, 8dp minimum separation. `Modifier.minimumTouchTarget()` exists
for this; icons smaller than 48dp must be wrapped in it.

Motion: 150–200ms, ease-out only. No bounce, no spring, no overshoot, no confetti, no pulsing.
The only continuous animation permitted is the countdown text updating once per minute.

---

## 5. Component API

These signatures are the contract. Implement them in `:design` before any screen consumes them.

```kotlin
@Composable
fun NextPrayerHero(
    prayerName: String,
    prayerNameArabic: String,
    time: String,
    remaining: String,
    heading: String,
    whyLabel: String,
    onWhyThisTime: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun PrayerRow(
    name: String,
    time: String,
    isNext: Boolean,
    modifier: Modifier = Modifier,
)

@Composable
fun ActionChip(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun PrimaryButton(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
)

@Composable
fun ObservanceRow(
    title: String,
    subtitle: String,
    marker: ObservanceMarker,
    alertEnabled: Boolean,
    alertDescription: String,
    onToggleAlert: () -> Unit,
    modifier: Modifier = Modifier,
)

enum class ObservanceMarker { RECOMMENDED_FAST, PROHIBITED_FAST, HIJAMAH }

@Composable
fun LabelledIconButton(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun Counter(
    current: Int,
    outOf: String,
    spokenDescription: String,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun CheckRow(
    label: String,
    status: CheckStatus,
    statusDescription: String,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
)

enum class CheckStatus { PASS, FAIL }

@Composable
fun SectionDivider(modifier: Modifier = Modifier)

@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
)
```

**`ScreenHeader` was missing from the original spec — that's a gap in this document, not
something implementation skipped.** It is mandatory on **every** screen, including the four
root tab destinations (Today, Calendar, Adhkār, More). A tab root takes it without `onBack`.

An earlier revision of this section exempted the tab roots on the grounds that they are not
"back"-able. That was the wrong test: the header is what gives every screen the same title in
the same place at the same size, and four roots each inventing their own heading — one at
`titleLarge`, one at `titleMedium`, one with no title at all — is what made the app read as
unfinished. Being back-able decides whether the header carries a back control, not whether the
header exists.

Rules:

- **Pinned at the very top of the screen, above all content, and it never scrolls away.** A back
  action placed at the bottom of scrolling content — reachable only after scrolling past
  everything else — does not satisfy this component. Use it for a secondary confirm action if a
  screen wants one; it is never the only way back.
- `onBack` renders as a small icon control inline in the header, RTL-mirrored per §7, not a
  full-width button competing visually with body content.
- `title` is always a localized string. A screen title in the wrong locale is the same bug as a
  hardcoded English label anywhere else — see §7.

**Both buttons take a glyph, and neither has an icon-only variant.** `ActionChip` is outlined in
`accent` at 1.5dp on a `surface` fill, pill radius, with the glyph before the label. A `line`
border over `surface` was tried and is wrong: both tokens sit within a hair of `bg`, so the
result reads as a faintly tinted word rather than as something to press. `PrimaryButton` is the
filled `accent` block for the one action a screen is asking for, its glyph and label reversed out
in `bg` — the pair every palette's accent is chosen to contrast against.

Rules that apply to all components:

- **Every interactive component takes a visible text label.** There is no icon-only variant of
  `LabelledIconButton`, and no primary flow contains a bare icon.
- No component reads a theme colour directly. Colours come from `SaadiahTheme.colors`.
- No component takes a `Modifier` that lets a caller override padding — internal padding is part
  of the component's contract.
- Preview functions exist for every component in LTR, RTL, light, dark, and font scale 2.0.

---

## 6. Screens

### 6.1 Today

The only screen with a hero. Vertical order, top to bottom:

1. **Hijri date**, `titleMedium`, in the user's numeral setting. Gregorian below it in
   `bodySmall` / `textSecondary`. Day of week spelled out.
2. `SectionDivider`.
3. **Hero.** Centred. "Next prayer" label in `body`/`textSecondary`; prayer name in `titleLarge`
   / `accent`, Latin and Arabic on one line separated by a middle dot; time in `display`;
   remaining time in `body`, **spelled out in full** — "in 2 hours 14 minutes", never "2h 14m".
   Below it, a bordered `LabelledIconButton` reading "Why this time?".
4. `SectionDivider`.
5. **Prayer list.** A vertical list of rows, not a horizontal strip. Name left, time right, both
   `titleSmall`. **The highlighted row is the *next* prayer — the one the hero names.** Marking
   the prayer most recently passed instead leaves the highlight one row behind the hero all day,
   which reads as a highlight that is stuck rather than one answering a different question.
   It carries a filled `surface` background with 8dp radius, a 1dp `accent` outline, a 4dp
   `accent` bar at the leading edge, and its time in `accent`. Fill alone is not enough: `surface`
   sits a few percent off `bg` in every palette, correct for a card and far too quiet for the one
   row on the screen that matters.
6. **Observances**, at most two, each an `ObservanceRow` with a bell toggle.
7. **Continue** rows — evening adhkār when appropriate, and the most recent reading thread.
8. **Tab bar** — four tabs, 26dp icons, `label` text under each. Active tab in `accent`.

Shia variant: the prayer list collapses to three rows — Fajr, Ẓuhrayn, ʿIshāʾayn — driven by
`CombineMode`. Same component, different data.

Scrolling is acceptable at large font scales. Do not compress to avoid it.

### 6.2 Calendar

**List is the default view. Grid is a genuinely different visualisation, not the same list with
an extra legend appended.** The current implementation renders the same row layout under both
tabs, with the grid tab additionally showing the legend as inline rows — that's the one existing
tab doing double duty, not two views. Fix this by building the two as actually distinct:

- **List** (default): rows in date order, one observance per row. Hijri day number (`titleSmall`)
  and weekday (`bodySmall`) in a fixed 44dp leading column; then the observance name in `body`,
  spelled out in words — "Fast — ʿArafah", "Do not fast — Eid al-Aḍḥā", "Ḥijāmah day"; then the
  Gregorian date in `bodySmall`; then a bell toggle. Never a bare coloured dot. No legend needed
  here — the words carry the meaning.
- **Grid**: an actual month grid — day-of-week columns, numbered day cells, one calendar month
  at a time. Each cell carries the Gregorian day number plus a marker for that day if it has one:
  filled circle for recommended fast, horizontal bar for prohibited, ring outline for hijāmah.
  A legend naming all three in words sits below the grid, fixed, not scrolling with the month.
  This view answers "what does this month look like at a glance" — the list answers "what's
  coming up and can I set an alert for it." If that distinction doesn't hold for a given view,
  it's the wrong view, not a labelling problem.

**Enrichments, in priority order:**

1. **Tap a day for a detail sheet** (`ScreenHeader` + content): full observance detail including
   its evidence reference from §8 of the implementation plan, that day's five prayer times, and
   — where relevant — the alert toggle. This is the single highest-value addition since it gives
   the grid view something to do beyond browsing.
2. **Jump to today** — a single action from anywhere in the calendar, list or grid, at any month.
3. **Per-day prayer time peek** inside the detail sheet from (1), rather than a separate feature.

**Month stepping is not an enrichment — it is what makes the grid a grid.** Previous and next
month are always reachable, spelled out in words rather than bare chevrons per §8, either side
of the Gregorian span. Without them "one calendar month at a time" means one month and no other,
and "jump to today" is the only navigation there is.

Conflicts are explained, not hidden. When Ayyām al-Bīḍ overlaps Tashrīq, show a note in
`bodySmall` / `textSecondary` saying so.

### 6.3 Quran reader

Header: surah name Arabic then Latin, `titleSmall`; juzʾ and page in `bodySmall`.

**Thread chips** directly under the header — a horizontal row of pills, active one outlined in
`accent`. Tapping switches the reading position. This is the app's distinguishing mechanic; it is
not a settings item.

Āyah blocks: Quranic text at `quran`, RTL, generous line height, āyah number inline in `accent`
at `label` size. The active āyah carries a 2dp `accent` bar on its start edge.

**Set as a page, not as a feed.** The reader is one continuous column of naskh with no rule
between verses — a muṣḥaf has none. Each āyah is closed by its number inside `﴿ ﴾` in `accent` at
`label` size, in the text run itself rather than in a column beside it, so it stays in reading
order in both directions. Justified.

**The basmalah is set apart, above the first āyah, centred in `accent`, with a rule beneath it.**
Tanzil prefixes it to the stored text of āyah 1 for every sura that opens with one, so rendering
the stored string as-is prints the opening *as part of* āyah 1 — which for al-Baqarah it is not.
The split is a numbering correction and belongs in `:core:content`, tested, not in the screen.
In al-Fātiḥah the basmalah **is** āyah 1 and is never split off.

**Reading position is remembered per sura** and restored on reopening — the last āyah at the top
of the screen, written only once scrolling settles. It is a position and nothing else: never
shown back as a count, a percentage, or a record of what was finished.

**A reading rail** runs down the leading edge showing how far into the sura the reader is. Its
denominator is the number of āyāt that can reach the top of the screen, not the total — dividing
by the total leaves the rail short of full at the end of every sura. **A back-to-first-āyah
control** appears once the reader has left the top and is spelled out in words, per §8.

**Font.** §3 specifies KFGQPC Hafs Uthmanic. Until that file is licensed and measured against the
APK budget, the reader falls back to the platform serif, which resolves to Noto Naskh for Arabic —
the right script, not yet the right face. This is a known gap, not the intended end state.

Attached notes render below their āyah: text notes in `bodySmall` behind a start-edge border;
voice notes as an inset `surface` panel with a play control, a static waveform, duration and
recording date. Both are 48dp-tall touch targets minimum.

Footer: `LabelledIconButton` for bookmark, note, record and search — labelled, not bare icons —
and the words "saved on this device" in `bodySmall` / `textTertiary`. That line is deliberate:
the privacy claim appears where data is created.

### 6.4 Adhkār

Search field at the top, `body` placeholder.

Section header names the set in Arabic (`titleMedium` / `accent`) with position beneath
("Evening adhkār · 4 of 22") in `bodySmall`.

The dhikr card: Arabic at `quran` minus 2sp, transliteration in `bodySmall` italic, translation
in `body`, and the source reference in `bodySmall` / `textTertiary` behind a divider. **No entry
renders without a source reference.**

The counter is a 96dp ring in `accent` with the count in `display` minus 24sp. The entire card
area is the tap target. Position persists across process death.

Footer: previous/next chevrons at 48dp, with a progress indicator between them where the current
item is a wider pill, not merely a different colour.

### 6.5 Why this time

A bottom sheet with `ScreenHeader`. Title `titleLarge`, always localized (currently ships in
English — "Why 5:32 PM?" — that's a §7 violation, not a §6.5 one, but flagged here since this is
where it was seen). A definition list of Method, Fajr/Isha angle, ʿAṣr madhhab, high-latitude
rule and tuning — label in `body`/`textSecondary` at the start, value in `body` at the end, one
per row with a `lineSubtle` divider. The value the user is most likely to want to change is
rendered in `accent`.

**The explanatory panel is prayer-aware. It is not one canned paragraph reused for every prayer.**
What currently ships explains Aṣr madhhab regardless of which prayer's sheet is open, which is
meaningless for Fajr or Isha — madhhab has no bearing on either. The content must branch:

- **Fajr, Isha**: explain in terms of the twilight angle and the high-latitude rule — these are
  the two variables that actually move these times, and at latitudes like Exeter's they can move
  by more than an hour. Name the rule in plain words ("your Fajr uses the middle-of-the-night
  approximation because true twilight doesn't occur at this latitude in summer") rather than just
  the setting's label.
- **Aṣr**: explain in terms of madhhab, as it does today.
- **Dhuhr, Maghrib**: these are not method-sensitive. If there's a gap from the user's masjid, say
  so plainly and point at tuning rather than implying a method choice would fix it.

Below the definition list, an inset `surface` panel gives the concrete alternative in words —
what the other setting would produce, and by how many minutes it differs — using whichever
variable is relevant per the branching above.

**Two actions, and "Match my masjid" must actually do something.** It currently opens to static
text about the Ḥanafī/standard Aṣr difference and nothing else — there is no entry point for the
user to input their own masjid's times and no solve happening. Build the real flow:

1. **Entry points**: this button, *and* a standalone "Match my masjid" item in Settings — a user
   should be able to run this once for all five prayers, not rediscover it prayer by prayer.
2. **Input**: a form with one time field per prayer, labelled by name, pre-filled with today's
   computed time as a starting point. The user overwrites any field where they have their masjid's
   printed time; fields left unchanged are not treated as a hard constraint on the solve.
3. **Solve**: reuse `PrayerCalculator.solve()` from `core/prayer` (T1.4 in the implementation
   plan) across method × madhhab × high-latitude rule, scored by total absolute error across the
   fields the user actually entered, with any residual difference applied as per-prayer minute
   tuning.
4. **Confirmation**: show the matched profile in the same definition-list style as the sheet
   above, with an explicit "Apply" action — nothing changes silently.

The compact tuning button beside "Match my masjid" remains for a user who wants to nudge a single
prayer by hand without running the full solve.

### 6.6 Alert check

Title `titleLarge`, detected device in `bodySmall`.

A list of `CheckRow`s. Pass rows carry a check glyph in `sage`; fail rows carry a triangle glyph
in `warning` **and** a "Fix" action pill — status is never conveyed by icon colour alone.

Below: a delivery history strip of ten segments, `sage` for on-time and `warning` for late, with
a sentence in `body` stating the counts in words underneath. The strip is decoration; the
sentence is the information.

Primary action: "Run a 60-second test", full width.

---

## 7. Language and direction

- Arabic is the primary locale. Religious copy is written in Arabic first and translated to
  English, never the reverse.
- `start`/`end` only. `scripts/check-rtl.sh` fails the build on `left`/`right`.
- Every screen has Roborazzi coverage in LTR and RTL at font scale 1.0 and 2.0 — four
  screenshots per screen, all four gating the build.
- Wrap mixed-direction runs in bidi isolates (`\u2066`–`\u2069`). Times inside Arabic sentences
  break first.
- ICU message format with all six Arabic plural categories. `count == 1` logic is a bug.

---

**Two surfaces are right-to-left whatever the language setting says.** Direction follows the
content, not the UI locale, and both of these are Arabic content sitting inside a possibly
English app:

- **The Quran reader.** The muṣḥaf reads right-to-left; laying it out the other way for an
  English reader puts each āyah's closing number on the wrong side of the verse it closes.
- **The Hijri date banner on Today.** The label is Arabic — "٩ محرم ١٤٤٨ هـ" — and in a
  left-to-right layout the year and the هـ that qualifies it land on opposite sides of the
  month name.

Both wrap their content in `LocalLayoutDirection provides LayoutDirection.Rtl` rather than
relying on the app language.

**Times a reader types are on the 24-hour clock, in both languages,** and are chosen from a
picker rather than typed. A masjid prints 05:12 and 17:20; a reader copying that should not
have to translate it into an AM/PM dial on the way in, nor should the app have to guess at
what shape they wrote. Displayed times elsewhere still follow the language — Arabic writes
ص and م.

## 8. Accessibility gates

These are build failures, not review comments.

- `ColorContrastTest` passes for every declared token pair.
- No text style below 14sp exists in `Tokens.kt`.
- Every screen renders without clipping at font scale 2.0 in both directions.
- Every interactive element has a content description and a ≥48dp target.
- No information is conveyed by colour alone anywhere — every marker carries a shape or a word.
- TalkBack traversal order matches visual order on every screen.

## 9. Forbidden

Ambient gradients. Drop shadows for decoration. Glassmorphism. More than two font weights.
Icon-only primary actions. Text below 14sp. Colour-only status. Verse-of-the-day cards. Streaks,
badges, or any progress gamification. Full-bleed photography. Mosque silhouettes, prayer beads,
or the Kaaba as decorative motifs. Calligraphy of the shahādah in the app icon.

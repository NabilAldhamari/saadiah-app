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
| `textTertiary` | `#78837F` | labels, metadata | 4.70:1 |
| `accent` | `#B8935A` | active state, primary action | 6.42:1 |
| `sage` | `#5E7A6B` | recommended-fast marker | 3.91:1 — **markers only, never text** |
| `warning` | `#C57358` | failed check, prohibited day | 5.22:1 |

### Light

| Token | Hex | Use | Contrast on `bg` |
| --- | --- | --- | --- |
| `bg` | `#F7F4EC` | screen background | — |
| `surface` | `#EDE6D3` | cards, inset panels | 1.2:1 (non-text) |
| `line` | `#DDD7C7` | dividers | decorative |
| `lineSubtle` | `#EAE5D8` | row separators | decorative |
| `text` | `#0E1614` | primary text | 16.7:1 |
| `textSecondary` | `#4A4A44` | supporting copy | 8.14:1 |
| `textTertiary` | `#6B665C` | labels, metadata | 5.19:1 |
| `accent` | `#7A5A22` | active state, primary action | 5.79:1 |
| `sage` | `#3E5A4A` | recommended-fast marker | 6.91:1 |
| `warning` | `#9A3F22` | failed check, prohibited day | 6.14:1 |

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
    onWhyThisTime: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun PrayerRow(
    name: String,
    time: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
)

@Composable
fun ObservanceRow(
    title: String,
    subtitle: String,
    marker: ObservanceMarker,
    alertEnabled: Boolean,
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
    target: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun CheckRow(
    label: String,
    status: CheckStatus,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
)

enum class CheckStatus { PASS, FAIL }

@Composable
fun SectionDivider(modifier: Modifier = Modifier)
```

Rules that apply to all of them:

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
   `titleSmall`. The current prayer's row has a filled `surface` background with 8dp radius and
   its time in `accent` — highlighted by fill *and* colour, never colour alone.
6. **Observances**, at most two, each an `ObservanceRow` with a bell toggle.
7. **Continue** rows — evening adhkār when appropriate, and the most recent reading thread.
8. **Tab bar** — four tabs, 26dp icons, `label` text under each. Active tab in `accent`.

Shia variant: the prayer list collapses to three rows — Fajr, Ẓuhrayn, ʿIshāʾayn — driven by
`CombineMode`. Same component, different data.

Scrolling is acceptable at large font scales. Do not compress to avoid it.

### 6.2 Calendar

**List is the default view. Grid is the alternative.** A segmented control at the top switches
them, list selected on first launch.

List rows: Hijri day number (`titleSmall`) and weekday (`bodySmall`) in a fixed 44dp leading
column; then the observance name in `body`, spelled out in words — "Fast — ʿArafah",
"Do not fast — Eid al-Aḍḥā", "Ḥijāmah day"; then the Gregorian date in `bodySmall`; then a bell
toggle. Never a bare coloured dot.

Grid view keeps the dot markers but every marker is additionally distinguished by shape: filled
circle for recommended fast, horizontal bar for prohibited, ring outline for hijāmah. A legend
below the grid names all three in words.

Conflicts are explained, not hidden. When Ayyām al-Bīḍ overlaps Tashrīq, show a note in
`bodySmall` / `textSecondary` saying so.

### 6.3 Quran reader

Header: surah name Arabic then Latin, `titleSmall`; juzʾ and page in `bodySmall`.

**Thread chips** directly under the header — a horizontal row of pills, active one outlined in
`accent`. Tapping switches the reading position. This is the app's distinguishing mechanic; it is
not a settings item.

Āyah blocks: Quranic text at `quran`, RTL, generous line height, āyah number inline in `accent`
at `label` size. The active āyah carries a 2dp `accent` bar on its start edge.

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

A bottom sheet. Title `titleLarge`. A definition list of Method, Fajr/Isha angle, ʿAṣr madhhab,
high-latitude rule and tuning — label in `body`/`textSecondary` at the start, value in `body` at
the end, one per row with a `lineSubtle` divider. The value the user is most likely to want to
change is rendered in `accent`.

Below it, an inset `surface` panel giving the concrete alternative in words: what the other
madhhab would produce, and by how many minutes it differs.

Two actions: a filled-outline "Match my masjid" occupying most of the width, and a compact
tuning button beside it.

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

# Saadiah — brand identity

Authoritative for the mark, icon, and wordmark. Companion to `docs/DESIGN.md`, which governs the
in-app design system — this document governs everything that represents the app outside of it:
the launcher icon, the Play Store listing, and any external material.

## 1. The mark

**Crescent-S** — two offset crescents whose shared negative space resolves into the letter S.
It ties the brand's first letter directly to the Hijri calendar, which is the app's least
common and most distinguishing feature. Confirmed as final over the three alternatives explored
earlier (mihrab dot, khātam star, moon dial).

The mark is a single continuous shape, one fill colour, no internal detail below icon size. It is
built on a 108×108 grid so it drops directly into Android's adaptive icon system:

```
M66.22,36.55 A13,13 0 1,0 54,54 A13,13 0 1,1 41.78,71.45
```

stroked at **12** with **round caps**, and no fill.

Two arcs on tangent circles — centres `(54,41)` and `(54,67)`, both radius 13, meeting at the
grid's centre `(54,54)`. Each arc sweeps 250°, so each reads as a crescent; the round caps are
the crescent horns, and a butt cap turns them into cut-off bands. Because it is stroked rather
than filled it stays one continuous shape with no internal detail, which is what has to survive
at 24px.

> **Corrected geometry.** The path previously recorded here —
> `M54,29 A25,25 0 1,0 54,79 A18,18 0 1,1 54,29 Z` — encloses no area and renders blank. Its
> endpoints are 50 apart; the first arc at r=25 is an exact semicircle, and the second declares
> r=18, which cannot span 50, so the SVG spec scales it up to 25. The two arcs become identical
> and the outline retraces itself. Every asset generated from it shipped as an empty field: the
> 512px Play icon, all five legacy PNGs, both legibility crops, and the leading edge of both
> wordmarks. The path above replaces it and renders the mark as described.

**Minimum size:** 24px. Below that the counters close visually — do not use the mark smaller
than 24px anywhere, including favicons. At stroke 12 the counters stay open at 24px; heavier
strokes were tried and close them.

**Clear space:** at least half the mark's own height, clear of any other element, on all sides.

**Do not:** recolour the mark to a gradient, add a drop shadow, rotate it, place it inside a
second container shape (the adaptive icon's own mask is the only container it needs), or crop it.

## 2. Colour

**Confirmed against `design/src/main/kotlin/app/saadiah/design/Tokens.kt`.** The field is the
app's own light background rather than a value chosen for the icon: the launcher mark and the
first screen behind it are the same cream. The earlier provisional values (`#F2EAD3` cream,
`#17140D` dark) were estimated from screenshots and are superseded.

| Use | Value | Token it comes from |
| --- | --- | --- |
| Mark, on light | `#3A2E12` | brand-only; not an in-app token |
| Mark, on dark / monochrome | `#F7F4EC` | light `bg` / dark `text` |
| Background field (icon, cream contexts) | `#F7F4EC` | light `bg` |
| Background field (dark contexts) | `#0E1614` | dark `bg` |

Measured with the WCAG 2.1 formula, same as every in-app pair:

| Pair | Ratio |
| --- | --- |
| `#3A2E12` on `#F7F4EC` | 12.10:1 |
| `#F7F4EC` on `#0E1614` | 16.71:1 |

Both clear the §8 non-text gate of 3:1 with a wide margin. `BrandContrastTest` in `:design`
holds them, so this table and the shipped assets cannot drift apart silently.

## 3. Adaptive icon

Android adaptive icons are two layers plus an optional third:

- **Background** (`@color/ic_launcher_background`): flat cream field, no transparency, no detail.
- **Foreground** (`ic_launcher_foreground`): the mark, positioned so it stays inside the 66dp
  safe circle centred on the 108×108 grid — OEM launchers mask this layer into a circle,
  squircle, rounded square, or other shape, and anything outside the safe circle may be clipped.
  The mark spans y 22..86 and x 35..73, against a safe band of 21..87.
- **Monochrome** (`ic_launcher_monochrome`, Android 13+): the mark's silhouette only, single
  fill, no colour — the system tints this to match the user's wallpaper-derived palette.
  **It is a separate drawable, not the foreground reused**: the foreground's stroke colour is
  fixed brown and would fight every wallpaper it landed on.

All three ship as native vector drawables in `android/app/src/main/res/`. No PNG is needed for
the adaptive icon itself — only for the Play Store listing icon and the legacy pre-Android-8
fallback.

Legacy PNG fallbacks are provided at 48/72/96/144/192px in `brand/export/preview/` for
reference; the legacy mipmap set an app ships is generated from the same source at build time or
via Android Studio's Image Asset tool — don't hand-maintain multiple PNG copies.

## 4. Play Store listing icon

512×512, flat, fully opaque (no alpha channel — Play Console rejects icons with transparency).
Provided at `brand/export/play-store/icon-512.png`, verified RGB and alpha-free.

## 5. Wordmark

Two lockups, mark plus name, one per script:

- **English** (`brand/wordmark/wordmark-en.svg`): mark on the leading (left) edge for LTR
  contexts — packaging, README headers, English store listing assets.
- **Arabic** (`brand/wordmark/wordmark-ar.svg`): mark on the leading (right) edge for RTL
  contexts, "سعدية" set in Noto Naskh Arabic — matches the spelling already used in-app.

Do not typeset "Saadiah" in a display or condensed face — the wordmark uses a plain serif so it
reads as a name, not a logotype trying to look like a logotype.

**The SVGs are the deliverable; there are no wordmark PNGs.** Export them with a renderer that
does complex-text shaping — a naive rasteriser leaves the Arabic letters disconnected, which is
worse than having no PNG at all.

## 6. Deliberately not used

Mosque silhouettes, prayer beads, the Kaʿbah, gold gradients, or shahādah calligraphy — any of
these on an app icon reads as either generic stock-Islamic-app iconography or carries handling
sensitivities that a launcher icon shouldn't carry. The crescent-S mark was chosen specifically
because it's ownable without being literal.

## 7. File index

```
brand/
├─ src/                          source SVGs, edit these, regenerate exports from them
│   ├─ mark.svg                  the mark alone, transparent
│   ├─ icon-square.svg           composited, for Play listing + legacy fallback
│   └─ icon-alt-mihrab.svg       rejected alternative, kept for reference
├─ wordmark/
│   ├─ wordmark-en.svg
│   └─ wordmark-ar.svg
└─ export/
    ├─ play-store/icon-512.png
    └─ preview/                  legacy PNG fallbacks, legibility crops, contact sheet

android/app/src/main/res/
├─ drawable/ic_launcher_foreground.xml
├─ drawable/ic_launcher_monochrome.xml
├─ mipmap-anydpi/ic_launcher.xml
└─ values/colors.xml             ic_launcher_background
```

The vector drawables under `res/` are the shipped mark. `brand/src/` is where it is edited;
keep the two in step — the path and stroke width appear in both.

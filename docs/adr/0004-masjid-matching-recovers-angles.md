# 4. Matching a masjid recovers twilight angles, not minute offsets

**Status:** accepted

## Context

"Match my masjid" takes times a reader copies from their masjid's timetable and finds settings
that reproduce them. It originally recorded the difference between the calculated time and the
observed one as a per-prayer constant, in minutes, via `PrayerAdjustments`.

That is right on the day it is measured and wrong afterwards, because the gap between two
twilight angles is itself seasonal — it widens as the sun's path flattens. Measured against
this calculator, Fajr at 18.0° against 19.5°:

| Place | Latitude | Gap across the year | Spread |
| --- | --- | --- | --- |
| Kuala Lumpur | 3°N | 6–7 min | 1 min |
| Cairo | 30°N | 7–9 min | 2 min |
| London | 51.5°N | 9–48 min | **48 min** |

A reader in London who matched their masjid in January had a Fajr that was 38 minutes out by
May. Near the equator the same offset is right all year, which is why the defect was invisible
in testing anywhere warm.

### What the libraries offer

`adhan2` exposes both mechanisms directly. `CalculationParameters` takes arbitrary `fajrAngle`
and `ishaAngle` doubles — it is not restricted to the named `CalculationMethod` presets — and
`PrayerAdjustments` carries a per-prayer minute offset. So the choice between them is ours to
make, not a limitation to work around.

The same split exists in the PrayTimes reference implementation
(<https://praytimes.org/docs/calculation>, <https://praytimes.org/docs/code>): `params` holds
the Fajr and ʿIshāʾ angles, and `tune()` applies fixed per-prayer offsets. Neither library
inverts a time back into an angle; that has to be done here.

### On deriving the whole day from two anchors

Two known times do not determine the rest of the schedule, and an algorithm claiming otherwise
would be inventing information. The prayers are set by independent parameters:

| Prayer | Set by | Recoverable from its own time? |
| --- | --- | --- |
| Fajr | depression angle before sunrise | yes — one angle |
| ʿIshāʾ | depression angle after sunset, or a fixed interval | yes — one angle |
| ʿAṣr | shadow-length ratio, i.e. the madhhab | yes — a discrete choice |
| Ẓuhr | solar transit | fixed astronomically; a difference is a constant |
| Sunrise, Maghrib | the horizon | fixed astronomically; a difference is a constant |

Fajr's angle says nothing about ʿIshāʾ's: they are two separate numbers in every published
method, related only by convention. Interpolating "between" two anchors — linearly or
otherwise — has no physical basis, because the day is not a scale between two points. What two
anchors legitimately give you is the two parameters those anchors govern.

## Decision

Recover the parameter each observed time is generated from, rather than the minutes it differs
by:

- **Fajr and ʿIshāʾ** — numerically invert the time to a depression angle (`TwilightAngleSolver`).
  Time is monotonic in the angle, so it bisects. Published times are rounded to the minute, so
  angle against time is a staircase and a whole band of angles yields the observed minute; both
  edges of the band are found and the angle taken from the middle of the tread.
- **ʿAṣr** — the madhhab, chosen by the existing candidate scan.
- **Ẓuhr, Sunrise, Maghrib** — a constant minute offset, which is the correct representation:
  these are not angle-driven, so a masjid differing here differs by its iqāma padding or its
  rounding, and that genuinely is a constant.

A recovered angle replaces that prayer's offset outright; the two are alternative accounts of
the same gap and keeping both would double-count it.

**Where no angle exists, the offset stays.** Above roughly 48° latitude in summer the sun never
reaches the twilight angle, the high-latitude rule fixes the time, and every angle gives the
same answer — London moves not at all between 18° and 19.5° in June. `TwilightAngleSolver`
detects this as an implausibly wide band and returns null rather than recording a number that
had no bearing on the result. The caller falls back to minutes.

## Consequences

Calibrated on 15 January at London and checked every month, worst error across the year:

| | worst error |
| --- | --- |
| Recovered angle | 3 min (May) |
| Constant offset | 38 min (May), 10 min (June, July) |

Held by `theRecoveredAngleHoldsAllYearWhereAConstantOffsetDoesNot` and
`aMatchMadeInWinterIsStillRightInSpring`, both of which assert the offset's failure as well as
the angle's success — the second is what makes the first worth the arithmetic.

The residual 3 minutes is the limit of what a minute-rounded observation can pin: near the
high-latitude threshold, sensitivity rises to tens of minutes per degree, so half a step of
rounding is several minutes. Reducing it further would need times to the second, which no
masjid publishes.

The solve costs about eighty extra `PrayerTimes` evaluations — two bisections of forty steps —
per twilight prayer entered. It runs once, on a button press, and is not on any hot path.

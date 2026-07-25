# Saadiah

Prayer times, Quran, and adhkār. No ads. No account. No servers. Works with the radio off.

Saadiah is a free Islamic companion app for Android. It is free in both senses: nothing to pay,
and licensed so it stays free.

## What it does not do

These are architectural facts, not promises. Several are enforced by CI on every commit.

- **No servers.** Prayer times are computed on your device from a city you pick out of a list
  bundled in the app. Nothing about you is sent anywhere.
- **No accounts, no user IDs, no advertising IDs.**
- **No analytics, no crash-reporting SDK, no advertising, no attribution SDK.** The build fails
  if any dependency introduces a network host that is not on the allowlist in
  `config/allowed-hosts.txt`.
- **Three permissions.** Notifications, exact alarms, and boot-completed. The microphone is
  requested only if you record a voice note, and the app works fully without it. Location is
  never required. `SYSTEM_ALERT_WINDOW` and background location are permanently forbidden.
- **No in-app purchases, ever.**

## Verifying that

Every release is reproducible. Rebuild it yourself and compare:

```bash
git checkout v0.1.0
./gradlew :android:app:assembleRelease
sha256sum android/app/build/outputs/apk/release/*arm64-v8a*.apk
```

The result must match the `SHA256SUMS` file attached to the corresponding GitHub Release. CI
performs this same check on every release and refuses to publish if the hashes diverge. If they
ever differ, the binary is not the source — tell us loudly.

## Building

Requirements: JDK 21, Android SDK 35.

```bash
./gradlew check                      # format, static analysis, lint, tests, coverage gates
./gradlew :android:app:assembleDebug
```

`./gradlew check` must pass before any commit. See `CLAUDE.md` for the working agreement.

## Project layout

| Module | Responsibility |
| --- | --- |
| `core/model` | Value types. No dependencies. |
| `core/prayer` | Prayer time computation, calculation methods, mosque timetable solver. |
| `core/calendar` | Hijri conversion, fasting and hijāmah observance rules. |
| `core/schedule` | Pure alarm-specification computation. |
| `core/content` | Quran and adhkār access, Arabic-normalised search. |
| `core/data` | Settings and encrypted backup. |
| `design` | Compose tokens, accessible type scale, shared components. |
| `android/app` | The application. |

## Content provenance

Bundled data is generated offline by the scripts in `tools/` and committed with checksums
recorded in `docs/data-provenance.md`. Sources and their terms:

- **Quranic text** — Tanzil.net, Creative Commons Attribution 3.0. The text may not be modified.
  Source is credited in the app's About screen with a link to https://tanzil.net.
- **Uthmanic font** — King Fahd Glorious Quran Printing Complex.
- **City database** — GeoNames, Creative Commons Attribution 4.0.
- **Prayer time astronomy** — the Adhan library, high-precision equations from Meeus,
  *Astronomical Algorithms*.
- **Hijri calendar** — Umm al-Qura published month lengths, 1356–1500 AH.

Adhkār entries carry a source reference. If you find an error in any religious text in this app,
please open an issue — it is treated as the highest-severity class of bug.

## Licence

GNU Affero General Public License v3, with an additional permission under section 7 allowing
distribution through app stores whose terms would otherwise conflict. See `COPYING`.

The trademark and domain are held separately from the code so that no acquirer can take the name
and ship a tracking build under it. The licence and the reproducible builds are what make that
guarantee checkable rather than merely stated.

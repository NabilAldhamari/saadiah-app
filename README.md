# Saadiah

[![ci](https://github.com/NabilAldhamari/saadiah-app/actions/workflows/ci.yml/badge.svg)](https://github.com/NabilAldhamari/saadiah-app/actions/workflows/ci.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](COPYING)

Prayer times, adhkār, and the daily reading. No ads. No account. No servers. Works with the
radio off.

Saadiah is a free Islamic companion app for Android. It is free in both senses: nothing to pay,
and licensed so it stays free.

## What it does

- **Prayer times**, computed on the device for any of 170,493 towns and cities. Calculation
  method is inferred from the country and can be overridden; the ʿAṣr madhhab and whether to
  combine Ẓuhrayn and ʿIshāʾayn are yours to set. A *why this time* screen shows the angles and
  rules behind the answer, and offers the alternative madhhab's ʿAṣr for comparison with your
  masjid.
- **Prayer alerts** at the time, optionally before it, and optionally before the window closes.
  Each prayer can be silenced on its own. An alert-check screen reports whether notifications
  and exact alarms are permitted and records how late each alert actually arrived.
- **A Hijri calendar** with fasting and ḥijāmah days marked, scoped by tradition. The Hijri day
  begins at Maghrib, not midnight.
- **Adhkār** for morning and evening, each entry carrying its source. You can add your own,
  which are kept in their own section and carry no source line, because their author is you.
- **Sūrat al-Baqarah** — its reported merits with their narrations, a reading counter that
  counts days rather than taps, and an optional daily or weekly reminder.
- **Reading al-Baqarah and Āl ʿImrān** in full, from the Tanzil text.
- **Arabic or English**, switching the whole app in place, right-to-left included.
- **Four themes**: follow the phone, warm light, warm dark, and white with green.

## What it does not do

These are architectural facts, not promises. Several are enforced by CI on every commit.

- **No servers.** Prayer times are computed on your device from a city you pick out of a list
  bundled in the app. Nothing about you is sent anywhere.
- **No accounts, no user IDs, no advertising IDs.**
- **No analytics, no crash-reporting SDK, no advertising, no attribution SDK.** The build fails
  if any dependency introduces a network host that is not on the allowlist in
  `config/allowed-hosts.txt`.
- **No microphone, no camera, no location, no contacts, no storage.**
- **No in-app purchases, ever.**

## Permissions

Four, all of them about telling you when a prayer has come in. Every one degrades gracefully:
refuse it and the app keeps working, with less warning.

| Permission | Why | If you refuse it |
| --- | --- | --- |
| `POST_NOTIFICATIONS` | Announcing a prayer, and the reading reminder. | The app runs and shows times; nothing is announced. |
| `USE_EXACT_ALARM` | An alert at the time, not some minutes after it. | — |
| `SCHEDULE_EXACT_ALARM` | The same, on releases that ask for it separately. | Alerts fall back to inexact wake-ups, which may arrive late in a doze window. |
| `RECEIVE_BOOT_COMPLETED` | Restoring the alarm schedule after a restart. | Alerts stop after a reboot until the app is opened. |

`SYSTEM_ALERT_WINDOW` and background location are permanently forbidden. Voice recording is
future work; when it arrives it will be optional, and it is not requested today.

## Building and Testing

Requirements: JDK 21, Android SDK 36. The Gradle wrapper is committed; always use `./gradlew` rather than a local Gradle.

### Development and Quality Gates

```bash
./gradlew :android:app:assembleDebug   # Assemble debug APK
./gradlew check                       # Format, static analysis, lint, unit tests, coverage gates
./gradlew koverHtmlReport              # Generate detailed HTML code coverage report
```

`./gradlew check` must pass cleanly before any commit. See `CLAUDE.md` for the working agreement.

### Building for Google Play Store (Release Bundle)

Google Play Console requires an **Android App Bundle (`.aab`)** for app publication.

To build the signed release bundle ready for Google Play Console upload:

```bash
KEYSTORE_PASSWORD="<store_password>" \
KEY_ALIAS="<key_alias>" \
KEY_PASSWORD="<key_password>" \
./gradlew :android:app:bundleRelease --no-daemon
```

- **Output Bundle**: `android/app/build/outputs/bundle/release/app-release.aab`
- **Upload to Play Console**: In the [Google Play Console](https://play.google.com/console), navigate to your app > **Release** > **Production** (or **Testing** > **Internal testing**), create a new release, and upload `app-release.aab`.

> [!NOTE]
> If signing credentials are not supplied via environment variables, Gradle produces an unsigned bundle (`app-release.aab` without signature metadata). Google Play requires signed bundles. The signing configuration looks for `keystore.jks` in the repository root by default.

### Building Release APKs (for Testing and Sideloading)

To produce optimized, shrinked, per-ABI release APKs (arm64-v8a, armeabi-v7a, x86_64):

```bash
KEYSTORE_PASSWORD="<store_password>" \
KEY_ALIAS="<key_alias>" \
KEY_PASSWORD="<key_password>" \
./gradlew :android:app:assembleRelease --no-daemon
```

- **Output APKs**:
  - `android/app/build/outputs/apk/release/app-arm64-v8a-release.apk`
  - `android/app/build/outputs/apk/release/app-armeabi-v7a-release.apk`
  - `android/app/build/outputs/apk/release/app-x86_64-release.apk`

### Quality and Privacy Guards

Saadiah enforces 6 automated verification guards locally and on CI:

```bash
scripts/check-apk-size.sh          # Verifies arm64 release APK is within 12 MB budget
scripts/check-baseline-profile.sh  # Verifies dexopt/baseline.prof is embedded
scripts/check-forbidden-strings.sh # Ensures zero unauthorized network hosts in binary
scripts/check-permissions.sh       # Enforces allowed permissions against merged manifest
scripts/check-rtl.sh               # Enforces right-to-left layout compliance
scripts/check-ui-strings.sh        # Catches hardcoded prose outside translation tables
```

### Reproducible Build Verification

Every release is byte-for-byte reproducible. Rebuild it yourself and verify:

```bash
git checkout v0.1.0
./gradlew :android:app:assembleRelease
sha256sum android/app/build/outputs/apk/release/*arm64-v8a*.apk
```

The result must match the `SHA256SUMS` file attached to the corresponding GitHub Release. CI performs this same check on every release and refuses to publish if the hashes diverge.

## Performance Budgets (CI-Enforced)

| Metric | Budget Limit | Enforced By |
| --- | --- | --- |
| Cold start, P50, mid-range device | ≤ 400 ms | Macrobenchmark suite |
| Today screen full state recompute | ≤ 3 ms | JVM benchmark test |
| One day of prayer times | ≤ 1 ms | JVM benchmark test |
| Quran FTS query over 6,236 āyāt, P95 | ≤ 25 ms | JVM benchmark test |
| City prefix search per keystroke | ≤ 8 ms | JVM benchmark test (`CityDatabaseTest`) |
| Release APK, arm64 split | ≤ 12 MB | `scripts/check-apk-size.sh` |
| Base install download size | ≤ 10 MB | `scripts/check-apk-size.sh` |

## Project layout

| Module | Responsibility |
| --- | --- |
| `core/model` | Value types. No dependencies. |
| `core/prayer` | Prayer time computation, calculation methods, mosque timetable solver. |
| `core/calendar` | Hijri conversion, fasting and ḥijāmah observance rules. |
| `core/schedule` | Pure alarm-specification computation. |
| `core/content` | Quran and adhkār access, Arabic-normalised search. |
| `core/data` | Settings, the city index, and the reader's own adhkār. |
| `design` | Compose tokens, accessible type scale, shared components. |
| `android/app` | The application. |

## Content provenance

Bundled data is generated offline by the scripts in `tools/` and committed with checksums
recorded in `docs/data-provenance.md`. Sources and their terms:

- **Quranic text** — Tanzil Project, Creative Commons Attribution 3.0. The text may not be
  modified, so it is copied verbatim and nothing is normalised at generation. The notice
  travels inside the packed file and is shown with the text. Sūrat al-Baqarah and Āl ʿImrān
  are bundled today; the rest follows.
- **City database** — GeoNames, Creative Commons Attribution 4.0, credited in Settings.
- **Adhkār** — Ḥiṣn al-Muslim as published in an openly licensed dataset; every entry renders
  with its reference.
- **Prayer time astronomy** — the Adhan library, high-precision equations from Meeus,
  *Astronomical Algorithms*.
- **Hijri calendar** — Umm al-Qura published month lengths, 1356–1500 AH.

Every āyah carries the SHA-256 of its own bytes, checked before the text is drawn. A mismatch
refuses to render rather than showing suspect scripture.

Religious text carries a source reference or it is not shown. Where a verified, openly licensed
source has not been found, the screen says so plainly instead of filling the gap. If you find an
error in any religious text in this app, please open an issue — it is treated as the
highest-severity class of bug.

## Licence

GNU Affero General Public License v3, with an additional permission under section 7 allowing
distribution through app stores whose terms would otherwise conflict. See `COPYING`.

The trademark and domain are held separately from the code so that no acquirer can take the name
and ship a tracking build under it. The licence and the reproducible builds are what make that
guarantee checkable rather than merely stated.

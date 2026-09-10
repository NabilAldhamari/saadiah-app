#!/usr/bin/env bash
set -euo pipefail

readonly APK=$(find android/app/build/outputs/apk/release -name '*arm64-v8a*.apk' -print -quit 2>/dev/null || true)

if [[ -z "${APK}" ]]; then
  echo "check-baseline-profile: no arm64 release apk found." >&2
  exit 1
fi

if command -v unzip >/dev/null 2>&1; then
  entries=$(unzip -l "${APK}")
elif command -v jar >/dev/null 2>&1; then
  entries=$(jar tf "${APK}")
elif command -v python3 >/dev/null 2>&1; then
  entries=$(python3 -c 'import zipfile, sys; print("\n".join(zipfile.ZipFile(sys.argv[1]).namelist()))' "${APK}")
else
  echo "check-baseline-profile: could not find unzip, jar, or python3 to inspect ${APK}." >&2
  exit 1
fi

if grep -q 'assets/dexopt/baseline.prof' <<< "${entries}"; then
  echo "check-baseline-profile: baseline profile present."
  exit 0
fi

echo "check-baseline-profile: FAILED — no baseline profile in the release apk." >&2
exit 1

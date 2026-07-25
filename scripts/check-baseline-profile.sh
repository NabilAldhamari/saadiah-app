#!/usr/bin/env bash
set -euo pipefail

readonly APK=$(find android/app/build/outputs/apk/release -name '*arm64-v8a*.apk' -print -quit 2>/dev/null || true)

if [[ -z "${APK}" ]]; then
  echo "check-baseline-profile: no arm64 release apk found." >&2
  exit 1
fi

if unzip -l "${APK}" | grep -q 'assets/dexopt/baseline.prof'; then
  echo "check-baseline-profile: baseline profile present."
  exit 0
fi

echo "check-baseline-profile: FAILED — no baseline profile in the release apk." >&2
exit 1

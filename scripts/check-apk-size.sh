#!/usr/bin/env bash
set -euo pipefail

readonly LIMIT_BYTES=$((12 * 1024 * 1024))
readonly APK=$(find android/app/build/outputs/apk/release -name '*arm64-v8a*.apk' -print -quit 2>/dev/null || true)

if [[ -z "${APK}" ]]; then
  echo "check-apk-size: no arm64 release apk found. Run :android:app:assembleRelease first." >&2
  exit 1
fi

size=$(stat -c%s "${APK}" 2>/dev/null || stat -f%z "${APK}")
printf 'check-apk-size: %s is %s bytes (limit %s)\n' "${APK}" "${size}" "${LIMIT_BYTES}"

if (( size > LIMIT_BYTES )); then
  echo "check-apk-size: FAILED — apk exceeds the size budget." >&2
  exit 1
fi

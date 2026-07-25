#!/usr/bin/env bash
set -euo pipefail

readonly ALLOWLIST="config/allowed-hosts.txt"
readonly APK=$(find android/app/build/outputs/apk/release -name '*arm64-v8a*.apk' -print -quit 2>/dev/null || true)

if [[ -z "${APK}" ]]; then
  echo "check-forbidden-strings: no arm64 release apk found." >&2
  exit 1
fi

hosts=$(strings "${APK}" \
  | grep -oiE 'https?://[a-z0-9._-]+' \
  | sed -E 's#^https?://##' \
  | tr '[:upper:]' '[:lower:]' \
  | sort -u || true)

status=0
while IFS= read -r host; do
  [[ -z "${host}" ]] && continue
  if grep -qxF "${host}" "${ALLOWLIST}"; then
    continue
  fi
  case "${host}" in
    *.android.com|schemas.android.com|www.w3.org|xml.org|apache.org|*.jetbrains.com) continue ;;
  esac
  echo "check-forbidden-strings: unexpected host in release binary: ${host}" >&2
  status=1
done <<< "${hosts}"

if (( status == 0 )); then
  echo "check-forbidden-strings: no unexpected hosts."
fi
exit "${status}"

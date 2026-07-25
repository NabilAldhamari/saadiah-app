#!/usr/bin/env bash
set -euo pipefail

readonly ALLOWLIST="config/allowed-hosts.txt"
readonly APK=$(find android/app/build/outputs/apk/release -name '*arm64-v8a*.apk' -print -quit 2>/dev/null || true)

if [[ -z "${APK}" ]]; then
  echo "check-forbidden-strings: no arm64 release apk found." >&2
  exit 1
fi

# An apk is a zip, so most of its text is deflated and invisible to a scan of the archive
# itself. Unpacking first is what makes this check mean anything. `grep -a` also avoids
# depending on `strings`, which is absent on some developer machines — where this script
# used to report success having read nothing at all.
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

if ! unzip -qq -o "${APK}" -d "${work}"; then
  echo "check-forbidden-strings: could not unpack ${APK}." >&2
  exit 1
fi

hosts=$(grep -rhaoiE 'https?://[a-z0-9._-]+' "${work}" \
  | sed -E 's#^https?://##' \
  | tr '[:upper:]' '[:lower:]' \
  | sort -u || true)

status=0
while IFS= read -r host; do
  [[ -z "${host}" ]] && continue
  if grep -qxF "${host}" "${ALLOWLIST}"; then
    continue
  fi
  # Hosts that appear only as text — documentation shortlinks and bug-report addresses
  # inside library error messages, licence headers, XML namespaces. None is ever contacted;
  # each was read out of the unpacked binary and traced to its source before being listed.
  case "${host}" in
    *.android.com|schemas.android.com|*.googlesource.com|www.w3.org|xml.org|*.apache.org) continue ;;
    *.jetbrains.com|goo.gle|issuetracker.google.com) continue ;;
  esac
  echo "check-forbidden-strings: unexpected host in release binary: ${host}" >&2
  status=1
done <<< "${hosts}"

if (( status == 0 )); then
  echo "check-forbidden-strings: no unexpected hosts."
fi
exit "${status}"

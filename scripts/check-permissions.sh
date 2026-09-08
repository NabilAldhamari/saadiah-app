#!/usr/bin/env bash
set -euo pipefail

# Every permission the shipped app asks for, checked against the list it is allowed to ask for.
#
# This reads the *merged* manifest, not the one in source. A dependency can add a permission
# through manifest merging without a line changing in this repository, and INTERNET is the one
# that arrives that way — it is implied by most networking libraries and by a good many SDKs
# that do not announce themselves as networking libraries. Reading `src/main/AndroidManifest.xml`
# would report a clean bill of health while the built app asked for it.
#
# The app claims to make no network call ever. That claim is only as good as its enforcement,
# and this is the enforcement: no INTERNET, and nothing else that was not deliberately added.

readonly ALLOWLIST="config/allowed-permissions.txt"
readonly FORBIDDEN=(
  "android.permission.ACCESS_NETWORK_STATE"
  "android.permission.ACCESS_WIFI_STATE"
  "android.permission.ACCESS_FINE_LOCATION"
  "android.permission.ACCESS_COARSE_LOCATION"
  "android.permission.ACCESS_BACKGROUND_LOCATION"
  "android.permission.SYSTEM_ALERT_WINDOW"
  "android.permission.READ_CONTACTS"
  "android.permission.READ_PHONE_STATE"
)

manifest=$(find android/app/build/intermediates -path '*merged_manifest/release*' -name 'AndroidManifest.xml' -print -quit 2>/dev/null || true)

if [[ -z "${manifest}" ]]; then
  echo "check-permissions: no merged release manifest found. Run :android:app:assembleRelease first." >&2
  exit 1
fi

requested=$(grep -oE 'android:name="android\.permission\.[A-Z_]+"' "${manifest}" \
  | sed -E 's/android:name="//; s/"//' \
  | sort -u)

status=0

# Named outright as well as allowlisted, so the diff that introduces one reads as a refusal
# rather than as an oversight in a list someone could have extended without thinking.
for permission in "${FORBIDDEN[@]}"; do
  if grep -qxF "${permission}" <<< "${requested}"; then
    echo "check-permissions: FAILED — ${permission} is in the merged manifest." >&2
    echo "  It is forbidden outright. Find which dependency merged it in with:" >&2
    echo "    ./gradlew :android:app:processReleaseMainManifest --info" >&2
    status=1
  fi
done

while IFS= read -r permission; do
  [[ -z "${permission}" ]] && continue
  if ! grep -qxF "${permission}" "${ALLOWLIST}"; then
    echo "check-permissions: FAILED — ${permission} is not in ${ALLOWLIST}." >&2
    echo "  Add it there with a README permission-table entry, or remove what pulled it in." >&2
    status=1
  fi
done <<< "${requested}"

if (( status == 0 )); then
  count=$(grep -c . <<< "${requested}")
  echo "check-permissions: ${count} permissions, all allowed."
fi

exit "${status}"

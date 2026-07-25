#!/usr/bin/env bash
set -euo pipefail

readonly PATTERNS=(
  'padding\(left *='
  'padding\(right *='
  'Arrangement\.Absolute'
  'Alignment\.TopLeft'
  'Alignment\.TopRight'
  'Alignment\.BottomLeft'
  'Alignment\.BottomRight'
  'textAlign *= *TextAlign\.(Left|Right)'
  'android:layout_marginLeft'
  'android:layout_marginRight'
  'android:paddingLeft'
  'android:paddingRight'
)

status=0
for pattern in "${PATTERNS[@]}"; do
  if matches=$(grep -rnE "${pattern}" --include='*.kt' --include='*.xml' core design android 2>/dev/null); then
    echo "check-rtl: direction-absolute usage found (use start/end instead):" >&2
    echo "${matches}" >&2
    status=1
  fi
done

if (( status == 0 )); then
  echo "check-rtl: no direction-absolute usage."
fi
exit "${status}"

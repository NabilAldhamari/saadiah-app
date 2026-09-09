#!/usr/bin/env bash
set -euo pipefail

# English prose reaching a screen without going through the Strings table has now shipped
# twice: once as a permissions claim, once as "Why 5:32 PM?" rendering inside the Arabic UI.
# The Strings interface makes a *missing* translation a compile error, but it cannot stop a
# literal being written straight into a composable, so that is what this catches.
#
# A literal is suspicious when it is inside UI code, holds two or more words of Latin letters,
# and is not in one of the translation tables themselves.
#
# :design is scanned too. It was not, and that is exactly where "Next prayer" and
# "Why this time?" sat untranslated on the first screen of the app while this check passed
# green. A component module cannot see the Strings table, so a literal there is not a shortcut
# to fix later — it is a string no translation can ever reach. Take the label as a parameter.
#
# Source citations are exempt. "Ṣaḥīḥ Muslim 596" is a reference, not copy: translating it
# would make it harder to check, which is the opposite of why it is shown.

readonly UI="android/app/src/main/kotlin/app/saadiah"
readonly DESIGN="design/src/main/kotlin/app/saadiah"

found=0
while IFS= read -r file; do
  case "$(basename "${file}")" in
    Strings.kt|EnglishStrings.kt|ArabicStrings.kt|ArabicNames.kt|SurahMetadata.kt) continue ;;
  esac
  # Two or more Latin words in a row inside a double-quoted literal.
  hits=$(grep -nE '"[^"]*[A-Za-z]{2,}[ ][A-Za-z]{2,}[^"]*"' "${file}" \
    | grep -vE '^[0-9]+: *(//|\*)' \
    | grep -vE 'Suppress|OptIn|contentDescription = null|import |package |^\s*\*' \
    | grep -vE '"[a-z.]+\.[a-z]+"'     | grep -vE 'Ḥiṣn|Ṣaḥīḥ|Nasāʾī|Muslim [0-9]|admin1 =' || true)
  if [[ -n "${hits}" ]]; then
    echo "check-ui-strings: prose literal outside the Strings table in ${file}" >&2
    echo "${hits}" | sed 's/^/    /' >&2
    found=1
  fi
done < <(find "${UI}" "${DESIGN}" -name '*.kt')

if (( found == 0 )); then
  echo "check-ui-strings: no prose literals outside the translation tables."
fi
exit "${found}"

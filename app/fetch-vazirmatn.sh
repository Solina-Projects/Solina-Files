#!/usr/bin/env bash
# Fetch Vazirmatn (SIL Open Font License 1.1) into app/src/main/res/font/.
# Same pattern as native/fetch-pdfium.sh: the TTF files are NOT committed,
# they are downloaded and SHA-256 verified at build time (CI runs this
# before Gradle). Re-running is a no-op when the files already match.
set -euo pipefail

BASE="https://raw.githubusercontent.com/rastikerdar/vazirmatn/master/fonts/ttf"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$ROOT/app/src/main/res/font"
mkdir -p "$DEST"

# Android file-based resource names must be lowercase a-z, 0-9 or underscore,
# so the downloaded files are installed as vazirmatn_regular.ttf etc. — the
# capital 'V' and hyphens in the upstream names broke :app:packageDebugResources
# ('V' is not a valid file-based resource name character). These are exactly
# the names Type.kt refers to as R.font.vazirmatn_*.
fetch() {
  local file="$1" sum="$2" name="$3"
  local target="$DEST/$name"
  # Drop a file installed by the pre-fix script under the upstream name.
  rm -f "$DEST/$file"
  if [ -f "$target" ] && echo "$sum  $target" | sha256sum -c - >/dev/null 2>&1; then
    echo "Vazirmatn $name already in place"
    return 0
  fi
  local tmp
  tmp="$(mktemp)"
  curl -fsSL --retry 4 -o "$tmp" "$BASE/$file"
  echo "$sum  $tmp" | sha256sum -c -
  cp "$tmp" "$target"
  rm -f "$tmp"
}

fetch Vazirmatn-Regular.ttf   b69fd4c680b8f3f225feabcc655a2c585d97627b8f5f5c0f9985e894069f3a56 vazirmatn_regular.ttf
fetch Vazirmatn-Medium.ttf    b986623e4ddef10755e04be39f8ea7bcb1dc08bfe8dd0aa6af395736f256ad4a vazirmatn_medium.ttf
fetch Vazirmatn-Bold.ttf      f635fdbea28f265de395ba83b4b1570dcf2f58d13c65469e61903b1c2d2ae723 vazirmatn_bold.ttf
fetch Vazirmatn-ExtraBold.ttf 5181c2e436cf1d5cf1c5631821b43d81881dca040a12c9a442ef5dc0007001cf vazirmatn_extrabold.ttf

echo "Vazirmatn in place"

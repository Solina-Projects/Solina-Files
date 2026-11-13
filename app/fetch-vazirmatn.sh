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

fetch() {
  local file="$1" sum="$2"
  local target="$DEST/$file"
  if [ -f "$target" ] && echo "$sum  $target" | sha256sum -c - >/dev/null 2>&1; then
    echo "Vazirmatn $file already in place"
    return 0
  fi
  curl -fsSL --retry 4 -o "$target" "$BASE/$file"
  echo "$sum  $target" | sha256sum -c -
}

fetch Vazirmatn-Regular.ttf   b69fd4c680b8f3f225feabcc655a2c585d97627b8f5f5c0f9985e894069f3a56
fetch Vazirmatn-Medium.ttf    b986623e4ddef10755e04be39f8ea7bcb1dc08bfe8dd0aa6af395736f256ad4a
fetch Vazirmatn-Bold.ttf      f635fdbea28f265de395ba83b4b1570dcf2f58d13c65469e61903b1c2d2ae723
fetch Vazirmatn-ExtraBold.ttf 5181c2e436cf1d5cf1c5631821b43d81881dca040a12c9a442ef5dc0007001cf

echo "Vazirmatn in place"

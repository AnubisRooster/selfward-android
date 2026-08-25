#!/bin/bash
# Renders the store artwork to exact pixel sizes.
#
# Headless Chrome rather than an SVG converter: the artwork is set in Lato,
# which is not installed locally, and Chrome will fetch it from Google Fonts.
# rsvg/cairosvg silently fall back to a system face and the wordmark comes out
# in the wrong typeface without any error.
set -euo pipefail
cd "$(dirname "$0")"
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"
out=".."

shot() { # <source html> <width> <output name>
  "$CHROME" --headless --disable-gpu --hide-scrollbars \
    --force-device-scale-factor=1 --virtual-time-budget=8000 \
    --window-size="$2",500 --screenshot="$out/$3" "file://$PWD/$1"
}

shot banner-1600x500.html          1600 banner-1600x500.png
shot site-banner-1600x500.html     1600 site-banner-1600x500.png
shot feature-graphic-1024x500.html 1024 feature-graphic-1024x500.png

echo "rendered into $(cd "$out" && pwd)"

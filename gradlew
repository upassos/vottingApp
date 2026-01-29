#!/usr/bin/env sh
# Minimal Gradle "wrapper" bootstrapper (no gradle-wrapper.jar).
# Downloads the Gradle distribution defined in gradle/wrapper/gradle-wrapper.properties and runs it.

set -eu

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$PROPS" ]; then
  echo "Missing $PROPS" >&2
  exit 1
fi

DIST_URL="$(grep -E '^distributionUrl=' "$PROPS" | sed 's/distributionUrl=//')"
# unescape https\:// -> https://
DIST_URL="$(printf "%s" "$DIST_URL" | sed 's#\\:##g')"

DIST_NAME="$(basename "$DIST_URL")"
DIST_DIR="$APP_HOME/.gradle-wrapper/dist"
ZIP_PATH="$DIST_DIR/$DIST_NAME"

mkdir -p "$DIST_DIR"

if [ ! -f "$ZIP_PATH" ]; then
  echo "Downloading Gradle distribution: $DIST_URL"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$DIST_URL" -o "$ZIP_PATH"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$DIST_URL" -O "$ZIP_PATH"
  else
    echo "Need curl or wget to download Gradle distribution." >&2
    exit 1
  fi
fi

# Unzip (idempotent)
UNPACK_DIR="$DIST_DIR/unpacked"
mkdir -p "$UNPACK_DIR"

# distribution zips have a single top-level directory gradle-<ver>
TOP_DIR="$(unzip -Z1 "$ZIP_PATH" | head -n 1 | cut -d/ -f1)"
if [ ! -d "$UNPACK_DIR/$TOP_DIR" ]; then
  echo "Unpacking $DIST_NAME"
  unzip -q "$ZIP_PATH" -d "$UNPACK_DIR"
fi

GRADLE_BIN="$UNPACK_DIR/$TOP_DIR/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  echo "Gradle binary not found at $GRADLE_BIN" >&2
  exit 1
fi

exec "$GRADLE_BIN" "$@"

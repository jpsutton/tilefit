#!/usr/bin/env bash
# Run the Gradle wrapper inside the build container (tools/docker/Dockerfile), so a host
# without the Android SDK can build and test:
#   scripts/docker-gradle.sh test lint assembleRelease
# The Gradle cache lives in ~/.cache/tilefit-gradle, so later runs are fast.
set -euo pipefail
src=$(cd "$(dirname "$0")/.." && pwd)
image=tilefit-build:latest
cache=${TILEFIT_GRADLE_CACHE:-$HOME/.cache/tilefit-gradle}
mkdir -p "$cache"

if ! docker image inspect "$image" >/dev/null 2>&1 || [ "${REBUILD_IMAGE:-}" = 1 ]; then
    docker build -t "$image" "$src/tools/docker"
fi

exec docker run --rm \
    -u "$(id -u):$(id -g)" \
    -e GRADLE_USER_HOME=/gradle \
    -v "$cache":/gradle \
    -v "$src":/src \
    "$image" ./gradlew --no-daemon "$@"

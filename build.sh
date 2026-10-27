#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk}"
./gradlew "$@" clean build
python tools/check_provenance.py
python tools/archive_build.py

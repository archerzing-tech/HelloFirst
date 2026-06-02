#!/bin/bash
set -e

echo "=== Building with Gradle ==="
./gradlew assembleDebug

echo ""
echo "=== DONE ==="
ls -lh app/build/outputs/apk/debug/*.apk

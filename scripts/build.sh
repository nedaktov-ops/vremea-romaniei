#!/bin/bash
export ANDROID_HOME="$HOME/Android/Sdk"
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$SCRIPT_DIR"

echo "==================================="
echo " VremeaRomâniei - Build Script"
echo "==================================="
echo ""

case "${1:-help}" in
  clean)
    echo "Cleaning project..."
    ./gradlew clean
    ;;
  build)
    echo "Building debug APK..."
    ./gradlew assembleDebug
    echo ""
    echo "APK location: app/build/outputs/apk/debug/"
    ;;
  release)
    echo "Building release APK..."
    ./gradlew assembleRelease
    echo ""
    echo "APK location: app/build/outputs/apk/release/"
    ;;
  install)
    echo "Installing debug APK to connected device..."
    ./gradlew installDebug
    ;;
  test)
    echo "Running tests..."
    ./gradlew test
    ;;
  lint)
    echo "Running lint checks..."
    ./gradlew lint
    ;;
  *)
    echo "Usage: ./scripts/build.sh [command]"
    echo ""
    echo "Commands:"
    echo "  clean    - Clean build artifacts"
    echo "  build    - Build debug APK"
    echo "  release  - Build release APK"
    echo "  install  - Install on connected device"
    echo "  test     - Run unit tests"
    echo "  lint     - Run lint checks"
    ;;
esac

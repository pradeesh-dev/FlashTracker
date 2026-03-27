#!/bin/bash
##############################################################################
# FlashTrack — Local Setup Script
# Run this once after cloning to set up the Gradle wrapper properly.
##############################################################################

set -e

GRADLE_VERSION="8.7"
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper-jar.jar"

echo "⚡ FlashTrack Setup"
echo "===================="

# Check Java
if ! command -v java &>/dev/null; then
    echo "❌ Java not found. Please install JDK 17+:"
    echo "   https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo "✅ Java ${JAVA_VERSION} found"

if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
    echo "⚠️  Java 17+ recommended. Current: ${JAVA_VERSION}"
fi

# Download gradle-wrapper.jar if needed
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "📥 Downloading gradle-wrapper.jar (v${GRADLE_VERSION})..."
    mkdir -p gradle/wrapper
    if command -v curl &>/dev/null; then
        curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
    elif command -v wget &>/dev/null; then
        wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
    else
        echo "❌ Please install curl or wget."
        exit 1
    fi
    echo "✅ gradle-wrapper.jar downloaded ($(du -sh $WRAPPER_JAR | cut -f1))"
else
    echo "✅ gradle-wrapper.jar present"
fi

chmod +x gradlew

echo ""
echo "📱 Building FlashTrack..."
./gradlew assembleDebug --no-daemon

echo ""
echo "✅ Build complete!"
echo "📦 APK: app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "Install to connected device:"
echo "  adb install app/build/outputs/apk/debug/app-debug.apk"

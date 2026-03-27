#!/bin/sh
##############################################################################
# Gradle wrapper script — auto-downloads gradle-wrapper.jar if missing
##############################################################################

APP_HOME="$(cd "$(dirname "$0")" && pwd -P)"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.7-wrapper-jar.jar"

# Download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    if command -v curl > /dev/null; then
        curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
    elif command -v wget > /dev/null; then
        wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
    else
        echo "ERROR: Please install curl or wget to download the Gradle wrapper."
        exit 1
    fi
fi

CLASSPATH="$WRAPPER_JAR"

# Determine JAVA_HOME
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
elif command -v java > /dev/null; then
    JAVA_CMD="java"
else
    echo "ERROR: Java not found. Please install JDK 17+."
    exit 1
fi

exec "$JAVA_CMD" \
    -classpath "$CLASSPATH" \
    "-Dorg.gradle.appname=gradlew" \
    org.gradle.wrapper.GradleWrapperMain "$@"

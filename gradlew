#!/bin/sh
# Gradle wrapper script for POSIX systems.
GRADLE_OPTS="${GRADLE_OPTS} \"-Xdock:name=Gradle\""
APP_HOME="$(cd "$(dirname "$0")" && pwd -P)"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java -classpath "$CLASSPATH" \
    -Dorg.gradle.appname="gradlew" \
    org.gradle.wrapper.GradleWrapperMain "$@"

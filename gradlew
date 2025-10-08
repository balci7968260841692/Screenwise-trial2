#!/bin/sh
# Minimal Gradle wrapper shim. Requires gradle-wrapper.jar already present.

DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Gradle wrapper jar missing. Please run 'gradle wrapper' with a full Gradle install." >&2
  exit 1
fi

JAVA_EXEC="${JAVA_HOME}/bin/java"
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_EXEC" ]; then
  JAVA_EXEC="java"
fi

exec "$JAVA_EXEC" -jar "$WRAPPER_JAR" "$@"

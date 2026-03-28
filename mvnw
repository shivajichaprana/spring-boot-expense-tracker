#!/bin/sh
# Maven Wrapper script - downloads and runs Maven
# See https://maven.apache.org/wrapper/

set -e

if [ -z "$JAVA_HOME" ]; then
    JAVACMD="java"
else
    JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    MAVEN_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

MAVEN_HOME="$HOME/.m2/wrapper/dists/$(basename "$MAVEN_URL" .zip)"

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven from $MAVEN_URL..."
    mkdir -p "$MAVEN_HOME"
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL "$MAVEN_URL" -o /tmp/maven.zip
    elif command -v wget > /dev/null 2>&1; then
        wget -q "$MAVEN_URL" -O /tmp/maven.zip
    fi
    unzip -q /tmp/maven.zip -d "$MAVEN_HOME"
    rm /tmp/maven.zip
fi

MVN_CMD=$(find "$MAVEN_HOME" -name "mvn" -path "*/bin/mvn" | head -1)

if [ -z "$MVN_CMD" ]; then
    echo "Error: Maven not found. Please install Maven manually." >&2
    exit 1
fi

exec "$MVN_CMD" "$@"

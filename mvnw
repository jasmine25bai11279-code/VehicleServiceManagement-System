#!/bin/sh
# Lightweight Maven wrapper script
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
elif [ -d "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

MVN_BIN="$DIR/../apache-maven-3.9.9/bin/mvn"
if [ ! -f "$MVN_BIN" ]; then
    MVN_BIN="/Users/arihantjain/.gemini/antigravity/scratch/apache-maven-3.9.9/bin/mvn"
fi

if [ -f "$MVN_BIN" ]; then
    exec "$MVN_BIN" "$@"
else
    command -v mvn >/dev/null 2>&1 && exec mvn "$@" || {
        echo "Maven binary not found. Please install maven or ensure apache-maven-3.9.9 is in the scratch folder."
        exit 1
    }
fi

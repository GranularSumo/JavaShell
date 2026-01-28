#!/bin/sh

set -e # Exit early if any commands fail

(
  cd "$(dirname "$0")" # Ensure compile steps are run within the repository directory
  ./gradlew -q jar
)

exec java --enable-preview --enable-native-access=ALL-UNNAMED -jar /tmp/javashell-build/javashell.jar "$@"

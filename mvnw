#!/usr/bin/env sh
set -eu
MAVEN_VERSION=3.9.9
WRAPPER_HOME="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="${WRAPPER_HOME}/apache-maven-${MAVEN_VERSION}"
if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  echo "Download Apache Maven ${MAVEN_VERSION} using the Maven Wrapper on Windows (mvnw.cmd), or install Maven on this system."
  exit 1
fi
exec "${MAVEN_HOME}/bin/mvn" "$@"

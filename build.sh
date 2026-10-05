#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn clean verify
mkdir -p dist
cp target/food-delivery.jar dist/food-delivery.jar
echo 'Build complete. Run bash run.sh.'

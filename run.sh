#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "Install JDK 17+ and add Java to PATH."; exit 1; }
[[ -f dist/food-delivery.jar ]] || { echo "Run bash build.sh first."; exit 1; }
exec java -jar dist/food-delivery.jar "$@"

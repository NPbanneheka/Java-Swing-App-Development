#!/usr/bin/env bash
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/com/gatepasspro/*.java && java -cp out com.gatepasspro.Main

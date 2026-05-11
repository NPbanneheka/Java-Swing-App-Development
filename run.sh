#!/bin/bash
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/com/gateflowpro/GateFlowPro.java && java -cp out com.gateflowpro.GateFlowPro

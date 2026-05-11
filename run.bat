@echo off
cd /d %~dp0
if not exist out mkdir out
javac -encoding UTF-8 -d out src\com\gateflowpro\GateFlowPro.java
if errorlevel 1 pause && exit /b
java -cp out com.gateflowpro.GateFlowPro
pause

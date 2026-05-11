@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\com\gatepasspro\*.java
if %errorlevel% neq 0 pause & exit /b %errorlevel%
java -cp out com.gatepasspro.Main
pause

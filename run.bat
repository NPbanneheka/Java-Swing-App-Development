@echo off
title GateFlowPro WPF
cd /d %~dp0
dotnet restore
dotnet run
pause

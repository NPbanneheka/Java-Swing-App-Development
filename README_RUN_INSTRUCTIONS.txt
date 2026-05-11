GateFlowPro WPF Desktop Application
===================================

Technology
----------
Frontend/UI   : C# WPF (.NET 8)
Database      : SQLite Local Database
Data privacy  : Data saves locally inside the desktop app folder/bin output folder.

Default Admin Login
-------------------
Username: admin
Password: admin123

How to Run in VS Code
---------------------
1. Extract this ZIP file.
2. Open the GateFlowPro_WPF folder using VS Code.
3. Make sure .NET 8 SDK is installed.
4. Open Terminal in VS Code.
5. Run these commands:

   dotnet restore
   dotnet run

How to Run using Visual Studio
------------------------------
1. Extract this ZIP file.
2. Open GateFlowPro.Wpf.csproj in Visual Studio 2022.
3. Restore NuGet packages if Visual Studio asks.
4. Press Start / F5.

Important Notes
---------------
- This is a WPF Windows desktop application, so run it on Windows.
- SQLite database file is created automatically: Data/gateflowpro.db inside the output folder.
- Vehicle No auto fill works using Vehicle Master.
- Delivery Note and Transport Schedule can be previewed and printed on blank sheets.
- This is the first professional WPF base version. We can next upgrade exact Nestlé paper layout, alignment, logo, table spacing, and official print sizes.

Main Modules
------------
1. Login / Create User Account
2. Dashboard
3. Delivery Note / Gate Pass
4. Transport Schedule
5. Vehicle Master
6. Search Records
7. Reports
8. My Profile with photo path selection
9. Users list

# GatePassPro - Java Swing Desktop Application

GatePassPro is a complete Java Swing desktop application for Gate Pass and Schedule Management.

## Main Features
- User Registration and Login
- User profile management with profile photo upload
- Dashboard with summary cards
- Gate Pass issue form
- Driver return / invoice seal verification
- Schedule issue and check workflow
- Search old gate passes and schedules
- Reports table
- Local file-based storage, no database setup required

## Requirements
- Java JDK 17 or newer
- VS Code
- Extension Pack for Java in VS Code

## How to Run in VS Code
1. Extract this ZIP file.
2. Open the extracted `GatePassPro` folder in VS Code.
3. Open Terminal in VS Code.
4. Run:

```bash
javac -encoding UTF-8 -d out src/com/gatepasspro/*.java
java -cp out com.gatepasspro.Main
```

## Default Admin Account
- Username: `admin`
- Password: `admin123`

## Normal User Registration
Users can create their own account from the Register screen.
They can also upload/change their own profile photo after logging in.

## Data Storage
Application data is saved inside:

```text
data/app-data.ser
```

Profile photos are copied to:

```text
data/profile_photos/
```

## Windows Run Option
Double-click `run.bat` after extracting the ZIP.

## Notes
This version is made as a clean starter full application. You can test it first, then we can improve layout, fields, reports, printing, database connection, and business rules step by step.

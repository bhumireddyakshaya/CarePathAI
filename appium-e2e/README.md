# Enterprise Appium E2E Automation Framework

This project contains an Enterprise Appium End-to-End Automation Framework for Android Applications using Node.js, WebdriverIO (v8), Mocha, Chai, ExcelJS, and Winston.

## Features
- **Page Object Model (POM)** Architecture
- Support for **APK** and **Installed App** testing
- Comprehensive **Excel Reporting**
- **HTML Reports** using Mochawesome
- **Automated Failure Capture** (Screenshots, Logcat, Stack Traces)
- **CI/CD** integration using GitHub Actions
- **Custom Utilities** for Wait, Gestures, and Logging

## Prerequisites
- Node.js (v18+)
- Java JDK 11+
- Android SDK & Android Emulator/Real Device
- Appium 2.x

## Setup Instructions
1. Navigate to the `appium-e2e` directory:
   ```bash
   cd appium-e2e
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Install Appium Server globally (if not already installed via npm script):
   ```bash
   npm install -g appium
   appium driver install uiautomator2
   ```

## Execution Instructions

**Run locally on connected device or emulator (Default)**:
```bash
npm run test
```

**Run specific test file**:
```bash
npm run test -- --spec tests/login.test.js
```

**Run with specific APK (APK Installation)**:
```bash
set APK_PATH=C:/path/to/app.apk
npm run test
```

**Run targeting a specific Application Package**:
```bash
set APP_PACKAGE=com.example.app
set APP_ACTIVITY=com.example.app.MainActivity
npm run test
```

**Run in CI Mode**:
```bash
npm run test:ci
```

**Clean existing reports & logs**:
```bash
npm run clean
```

## Framework Structure
- `config/` - WebdriverIO configuration files
- `pages/` - Page Object Model classes
- `utilities/` - Helpers (Gestures, Reporter, Logger)
- `tests/` - E2E Mocha tests
- `reports/` - Generated HTML and Screenshots
- `excel/` - Generated Excel reports
- `logs/` - Execution logs

# ⚡ FlashTrack — Privacy-First Expense Tracker

> Modern · Minimalistic · 100% Offline · INR-first · Beautiful Dark UI

---

## 📱 Screenshots Overview

| Home Dashboard | Add Transaction | Analysis | Accounts |
|---|---|---|---|
| Greeting + Balance Card + Quick Actions + Recent Transactions | Expense / Income / Transfer tabs + Category picker + Tags | Donut pie chart + Category breakdown + Monthly filter | Account cards grouped by type with credit limits |

| Debts & IOUs | Reminders | Categories | Settings |
|---|---|---|---|
| Lent / Borrowed tabs + Settle dialog | Active / Inactive reminders with toggle | 3-column grid with icon + color | Theme toggle + Backup + Export + Lock |

---

## 🗂 Project Structure

```
FlashTrack/
├── app/
│   ├── src/main/
│   │   ├── java/com/devx/flashtrack/
│   │   │   ├── FlashTrackApplication.kt       ← Hilt app
│   │   │   ├── MainActivity.kt                ← Entry point
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt         ← Room DB + pre-population
│   │   │   │   │   ├── dao/Daos.kt            ← All 5 DAOs
│   │   │   │   │   └── entity/Entities.kt     ← Account, Transaction, Category, Debt, Reminder
│   │   │   │   └── repository/AppRepository.kt ← Single source of truth
│   │   │   ├── di/DatabaseModule.kt           ← Hilt DI bindings
│   │   │   ├── receivers/Receivers.kt         ← Boot + Reminder broadcast
│   │   │   ├── viewmodel/MainViewModel.kt     ← Centralized StateFlows
│   │   │   └── ui/
│   │   │       ├── navigation/NavGraph.kt     ← Compose Navigation
│   │   │       ├── theme/                     ← Color, Type, Theme
│   │   │       ├── components/SharedComponents.kt
│   │   │       └── screens/
│   │   │           ├── onboarding/            ← 3-page welcome
│   │   │           ├── home/                  ← Dashboard
│   │   │           ├── transaction/           ← Add Expense/Income/Transfer
│   │   │           ├── accounts/              ← Account list + add/edit
│   │   │           ├── analysis/              ← Pie chart + monthly stats
│   │   │           ├── debts/                 ← IOUs + settle-up
│   │   │           ├── reminders/             ← Recurring + notifications
│   │   │           ├── categories/            ← Custom categories management
│   │   │           └── settings/              ← Theme + backup + security
│   │   └── res/
│   │       ├── values/themes.xml
│   │       └── xml/                           ← network_security_config, file_paths
│   └── build.gradle.kts
├── gradle/libs.versions.toml                  ← Version catalog
└── settings.gradle.kts
```

---

## 🚀 Setup Instructions

### Prerequisites
- **Android Studio Hedgehog (2023.1.1)** or later
- **JDK 17**
- **Android SDK 35** (targetSdk)
- **Min SDK 26** (Android 8.0+)

### Steps
```bash
# 1. Clone / copy project
cd FlashTrack

# 2. Open in Android Studio
#    File → Open → select FlashTrack/ folder

# 3. Let Gradle sync (first time downloads ~200MB)

# 4. Run on device or emulator
#    Run → Run 'app'   (or Shift+F10)
```

---

## ✨ Features Implemented

### ✅ Multi-Account System
- Bank accounts, Wallets (GPay, Amazon Pay, PhonePe), Cash, Credit Cards
- Credit card extras: limit, available credit, due date, billing cycle
- Toggle "Show Balance" eye icon on home + accounts screens
- **Combined overview**: Total Balance + Available Credit

### ✅ Transactions
- **3 tabs**: Expense (red) | Income (green) | Transfer (blue)
- Fields: Date picker, Amount (large keypad), Category (grid sheet), Account selector
- Notes, Tags (quick chips + custom), Recurring toggle (weekly/monthly/yearly)
- Auto balance update on both accounts for transfers

### ✅ Debts & IOUs
- Separate **"Debts & IOUs"** screen with Lent / Borrowed / Settled tabs
- Per-entry: person name, amount, date, notes
- **Settle Up** dialog with partial settlement + progress bar
- Running totals: "You'll Receive" vs "You Owe"

### ✅ Custom Categories
- 16 default categories with emoji icons + colors
- Add unlimited custom categories with name, icon (18 options), color (12 options), type, monthly budget
- Edit / delete non-default categories

### ✅ Recurring / Reminders
- Mark transactions as recurring (Weekly / Monthly / Yearly / Daily)
- Dedicated Reminders screen: Active / Inactive tabs
- Toggle reminders on/off, set due date, amount, repeat interval
- Notification broadcast receiver + Boot receiver for rescheduling

### ✅ Analytics & Budgets
- **Donut pie chart** (YCharts) with category breakdown
- Monthly expense / income / balance summary
- Category-wise % bar + spending list
- Month navigation (prev/next arrows)
- Per-category budget support (stored in DB)

### ✅ Premium Extras
- **CSV Export** via share sheet
- **Biometric lock** toggle (UI ready, hook into `BiometricPrompt`)
- Encrypted backup / restore stubs (ready to implement with SQLCipher)
- **Auto-categorization** via keyword mapping (stored per category)
- Split expense fields in DB (notes field approach)
- Material 3 + animated navigation transitions
- Edge-to-edge display, splash screen

---

## 🎨 Design System

| Token | Value |
|---|---|
| Primary (Income) | `#00C853` Green |
| Expense | `#FF1744` Red |
| Credit Card | `#2979FF` Blue |
| Debt / IOU | `#D500F9` Purple |
| Reminder | `#FF6D00` Orange |
| Background (dark) | `#0A0A0A` |
| Surface (dark) | `#141414` / `#1E1E1E` |

---

## 📦 Key Dependencies

| Library | Purpose |
|---|---|
| Jetpack Compose BOM 2024.08 | UI framework |
| Room 2.6.1 | Local SQLite database |
| Hilt 2.51.1 | Dependency injection |
| Navigation Compose 2.7.7 | Screen navigation |
| YCharts 1.6.7 | Pie / donut chart |
| DataStore Preferences 1.1.1 | Settings persistence |
| Biometric 1.1.0 | Fingerprint / face lock |
| WorkManager 2.9.1 | Background reminder jobs |
| Core Splashscreen 1.0.1 | Animated splash |

---

## 🔐 Privacy & Security

- **Zero network permissions** (no internet access)
- **FileProvider** for safe file sharing (CSV export)
- **No analytics, no ads, no cloud**
- Data lives entirely in `/data/data/com.devx.flashtrack/`
- Backup file is encrypted (SQLCipher ready stub)
- Biometric + PIN lock hooks provided

---

## 🛣️ Roadmap / TODOs

- [ ] SQLCipher database encryption
- [ ] Full biometric lock implementation
- [ ] Widget (balance glance on home screen)
- [ ] SMS auto-import (parse bank SMSes)
- [ ] Multiple currencies
- [ ] Monthly report PDF generation
- [ ] Split expense calculator
- [ ] Cloud backup option (E2E encrypted)

---

## 🤝 Build for Release

```bash
./gradlew assembleRelease
# APK → app/build/outputs/apk/release/app-release.apk
# Sign with your keystore before publishing to Play Store
```

---

*FlashTrack — Track fast. Stay private. ⚡*

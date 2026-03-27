# ⚡ FlashTrack — Privacy-First Expense Tracker

> Modern · Minimalistic · 100% Offline · INR-first · Dark-first · Jetpack Compose

---

## 🚀 Quick Start

### Option A — Android Studio (easiest)

```bash
# 1. Unzip and open
unzip FlashTrack_v2.zip
# File → Open → select FlashTrack/ folder in Android Studio

# 2. Let Gradle sync (first run downloads ~250 MB)
# 3. Run ▶ on any device / emulator (API 26+)
```

> **Android Studio handles the Gradle wrapper automatically.**

---

### Option B — Command Line

```bash
unzip FlashTrack_v2.zip && cd FlashTrack

# One-time setup: download the gradle wrapper jar
chmod +x setup.sh && ./setup.sh

# Subsequent builds
./gradlew assembleDebug

# Install to connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

### Option C — GitHub Actions CI

Push to GitHub — the included workflow (`.github/workflows/build.yml`) will:
1. Install JDK 17
2. Use `gradle/actions/setup-gradle` to generate the wrapper JAR automatically
3. Build the debug APK and upload it as a workflow artifact

**No need to commit `gradle-wrapper.jar`** — the CI workflow regenerates it from scratch.

---

## ⚠️ gradle-wrapper.jar Note

The `gradle-wrapper.jar` binary is not committed (standard practice for .gitignore).  
The project provides **three ways** to obtain it automatically:

| Method | How |
|---|---|
| Android Studio | Downloads it automatically on first sync |
| `./setup.sh` | Downloads from `services.gradle.org` |
| GitHub Actions | `gradle/actions/setup-gradle@v3` generates it |

---

## 📱 Features

### Multi-Account System
- Bank accounts, Wallets (GPay, PhonePe, Amazon Pay), Cash, Credit Cards
- Credit card fields: limit, available credit, billing cycle, due date
- Toggle "Show Balance" eye icon — hides all amounts
- Combined overview: Total Balance + Available Credit

### Transactions
- **3 types**: Expense (red) | Income (green) | Transfer (blue)
- Fields: Amount (large input), Title, Category (emoji grid), Account, Date, Notes, Tags
- Quick tag chips: amazon, netflix, zomato, grocery, bills, travel...
- Recurring toggle: Daily / Weekly / Monthly / Yearly

### Debts & IOUs
- Track money lent **and** borrowed
- Partial settlements with progress bar
- "Settle Up" dialog per person
- Running totals: "You'll Receive" vs "You Owe"

### Custom Categories
- 16 default categories with emoji icons
- Add unlimited custom categories
- Per-category: name, emoji icon, accent color, type (Expense/Income/Both), monthly budget

### Reminders
- Active / Inactive tabs
- Repeat intervals: Once / Daily / Weekly / Monthly / Yearly
- Due date picker, amount field
- Notification broadcast receiver + Boot receiver

### Analytics
- Monthly donut pie chart (YCharts)
- Category breakdown with % progress bars
- Month-by-month navigation (← →)
- Income / Expense / Balance summary pills

### Settings
- Dark/Light theme toggle
- Biometric lock toggle
- CSV export via share sheet
- Backup & Restore stubs (ready to wire up SQLCipher)
- About + Privacy policy

---

## 🗂 Architecture

```
FlashTrack/
├── data/
│   ├── local/
│   │   ├── entity/        ← Room @Entity data classes (Account, Transaction, Category, Debt, Reminder)
│   │   ├── dao/           ← 5 DAOs with Flow queries
│   │   └── AppDatabase.kt ← Room DB + seed data
│   └── repository/        ← AppRepository (single source of truth, balance updates)
├── di/                    ← Hilt DatabaseModule
├── viewmodel/             ← MainViewModel (StateFlows, flatMapLatest for month analytics)
├── ui/
│   ├── theme/             ← Color, Type, Theme (dark-first M3)
│   ├── components/        ← Shared helpers (EmptyState, iconEmojiFor)
│   ├── navigation/        ← NavGraph + Screen sealed class
│   └── screens/           ← 9 screens (Home, AddTransaction, Accounts, Analysis, Debts, Reminders, Categories, Settings, Onboarding)
└── receivers/             ← ReminderReceiver, BootReceiver
```

---

## 🎨 Design Tokens

| Role | Color |
|---|---|
| Income / Primary | `#00C853` Green |
| Expense | `#FF1744` Red |
| Credit Card | `#2979FF` Blue |
| Debts / IOU | `#D500F9` Purple |
| Reminder | `#FF6D00` Orange |
| Background (dark) | `#0A0A0A` |
| Surface (dark) | `#1E1E1E` |

---

## 📦 Key Dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.08.00 | UI framework |
| Material 3 | BOM-managed | Design system |
| Room | 2.6.1 | SQLite ORM |
| Hilt | 2.51.1 | Dependency injection |
| Navigation Compose | 2.7.7 | Screen routing |
| YCharts | 1.6.7 | Donut pie chart |
| DataStore Preferences | 1.1.1 | Settings persistence |
| Biometric | 1.1.0 | Fingerprint/Face lock |
| WorkManager | 2.9.1 | Background tasks |
| Core Splashscreen | 1.0.1 | Animated splash |

---

## 🔐 Privacy

- **Zero internet permissions** — fully air-gapped
- All data in `/data/data/com.devx.flashtrack/` — never leaves device
- FileProvider for safe CSV sharing
- No analytics, no crash reporting, no ads

---

## 📋 Requirements

| Item | Requirement |
|---|---|
| Android | API 26+ (Android 8.0+) |
| JDK | 17+ |
| Android Studio | Hedgehog 2023.1.1+ |
| Gradle | 8.7 (auto-downloaded) |
| AGP | 8.5.2 |

---

*FlashTrack — Track fast. Stay private. ⚡*

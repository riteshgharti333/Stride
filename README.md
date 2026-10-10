# Stride — Premium Minimalist Step Counter

**Stride** is an ultra-minimalist, battery-optimized Android step counter built with Jetpack Compose, Room Database, and modern Material 3 design. It combines precise hardware step tracking with a clean, Scandinavian-inspired White, Cobalt Blue, and Crimson Red aesthetic.

---

## ✨ Design Philosophy

- **Extreme Minimalism:** Generous whitespace, refined typography, and strict visual hierarchy with no unnecessary clutter or intrusive cards.
- **Color Identity:**
  - **White:** Clean, pristine card surfaces and background.
  - **Cobalt Blue (`#2563EB`):** Primary tracking metrics, active progress arc, and navigation indicators.
  - **Crimson Red (`#EF4444`):** Energy (kcal), goal milestone markers, and target threshold reference lines.
- **Privacy & Offline First:** Zero advertisements, zero accounts, zero analytics, and zero network calls. All data remains exclusively on your device.

---

## 🚀 Key Features

### 1. Today Screen (Daily Steps)
- **Large Step Typography:** High-contrast step count in elegant sans-serif styling.
- **Custom Canvas Progress Ring:** Smooth circular arc with rounded caps and dynamic percentage completion.
- **Daily Goal Progress:** Instant feedback on remaining steps and goal achievement celebrations.
- **Derived Metrics:** Real-time distance calculation in kilometers (based on configurable stride length) and active calories burned (kcal).
- **Simulation Mode:** Built-in testing controls (+50 / +500 steps) for emulator and desk testing.

### 2. Weekly Statistics
- **7-Day Minimalist Bar Chart:** Monday-to-Sunday overview rendered directly on Compose Canvas.
- **Goal Threshold Reference Line:** Dashed crimson red goal target line across the week.
- **Interactive Breakdown:** Tap any individual day's bar to inspect exact steps, distance, and calories.
- **Summary Metrics:** Total weekly steps, daily averages, most active day, and goal hit rates.
- **Week Navigation:** Effortlessly browse past weeks and trends.

### 3. Daily History
- **Chronological Activity Log:** Browse historical activity records safely persisted across app restarts.
- **Goal Met Badges:** Quick visual indicators for days that achieved the daily step target.
- **Graceful Empty State:** Friendly placeholder when no previous data is recorded.

### 4. Settings & Personalization
- **Adjustable Daily Goal:** Flexible stepper slider from 2,000 to 30,000 steps (default: 8,000).
- **Custom Stride Length:** Adjustable from 50 cm to 100 cm (default: 74 cm / ~29 in) for accurate distance estimation.
- **Theme Selection:** System Default, Light (White), and Midnight Dark.
- **Hardware Sensor Diagnostics:** Live inspection of sensor name, vendor, power rating (mA), and batching capability.
- **Continuous Background Tracking Toggle:** Low-power persistent service option for screen-off walking.

### 5. Home-Screen Widget
- Compact `AppWidgetProvider` showing today's live steps, daily goal, and a progress bar right from your home screen.

---

## 🔋 Battery Optimization & Architecture

Battery efficiency is at the core of Stride's engineering:

1. **Hardware Step Counter Coprocessor (`Sensor.TYPE_STEP_COUNTER`):**
   - Uses the dedicated low-power Sensor Hub DSP built into modern mobile chipsets.
   - Operates with **60-second hardware FIFO batching**, allowing the main Application Processor (AP) to remain asleep during long walks.
2. **Zero GPS Policy:** Never requests or accesses GPS / location hardware, eliminating the primary source of fitness-app battery drain.
3. **No Accelerometer Processing:** Avoids continuous sensor math loops.
4. **Debounced Flash Storage:** Sensor readings are held in memory and debounced to Room SQLite only upon significant step deltas (10+ steps) or app lifecycle transitions, preventing flash I/O battery consumption.
5. **Robust Reboot Recovery:** Automatically detects sensor zeroing after device reboots, preserving cumulative daily step totals seamlessly.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Reactive Data:** Kotlin Coroutines & `StateFlow`
- **Local Persistence:** Room Database (`SQLite`) + Encrypted SharedPreferences
- **Sensors:** Android `SensorManager` (`TYPE_STEP_COUNTER` & `TYPE_STEP_DETECTOR`)
- **Android Target:** minSdk 26, targetSdk 36
- **Testing:** JUnit 4 + Robolectric local JVM unit tests

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/
│   ├── MainActivity.kt               # Single Activity with M3 Navigation
│   ├── data/
│   │   ├── local/
│   │   │   ├── DailyStepEntity.kt    # Room entity for calendar days
│   │   │   ├── DailyStepDao.kt       # Room DAO queries & flows
│   │   │   └── AppDatabase.kt        # Room database singleton
│   │   └── preferences/
│   │       └── UserPreferences.kt   # Persistent goal, stride, and theme prefs
│   ├── sensor/
│   │   └── StepTrackerManager.kt     # Hardware sensor listener & reboot math
│   ├── service/
│   │   └── StepTrackingService.kt    # Guarded low-power Foreground Service
│   ├── receiver/
│   │   └── BootReceiver.kt           # BOOT_COMPLETED receiver
│   ├── widget/
│   │   └── StrideAppWidgetProvider.kt# Android home-screen widget provider
│   └── ui/
│       ├── MainViewModel.kt          # UI state holder & business logic
│       ├── components/
│       │   ├── MinimalProgressRing.kt    # Custom Canvas circular progress arc
│       │   ├── MinimalWeeklyBarChart.kt  # Custom Canvas 7-day bar chart
│       │   └── MetricCard.kt             # Minimalist stat tile component
│       ├── screens/
│       │   ├── HomeScreen.kt             # Daily steps hero screen
│       │   ├── WeeklyScreen.kt           # Weekly analytics & charts
│       │   ├── HistoryScreen.kt          # Chronological activity history
│       │   └── SettingsScreen.kt         # Preferences & diagnostics
│       └── theme/
│           ├── Color.kt              # White, Cobalt Blue, Crimson Red tokens
│           ├── Theme.kt              # Material 3 light/dark color schemes
│           └── Type.kt               # Minimalist typography hierarchy
└── res/
    ├── drawable/
    │   ├── ic_launcher_background.xml# Minimalist white gradient background
    │   ├── ic_launcher_foreground.xml# Geometric Blue & Red stride motif
    │   └── widget_background.xml     # Clean white card shape
    └── layout/
        └── widget_stride.xml         # RemoteViews widget layout
```

---

## 🔐 Permissions

| Permission | Purpose |
|---|---|
| `ACTIVITY_RECOGNITION` | Required by Android 10+ to access the hardware step counter sensor. |
| `RECEIVE_BOOT_COMPLETED` | Recalibrates sensor baseline and resumes tracking after device reboot. |
| `FOREGROUND_SERVICE` | Enables optional silent tracking with screen off. |
| `FOREGROUND_SERVICE_HEALTH`| Required by Android 14+ for health-related foreground services. |
| `POST_NOTIFICATIONS` | Displays the silent daily step notification when background service is active. |

---

## 🏃 Build & Run

To build the project using Gradle:

```bash
# Assemble Debug APK
gradle assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest
```

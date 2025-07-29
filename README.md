# PocketADHD - ADHD Assistant App

A Compose Multiplatform ADHD support app designed for teens and adults, focusing on attention, routines, mood tracking, and optional medication management. Built with offline-first architecture, encrypted local storage, and ADHD-friendly design principles.

## 🎯 Project Overview

**Target Platforms:** Android + iOS  
**Architecture:** MVVM with unidirectional state flow  
**Navigation:** Decompose (root/child components, back stack)  
**DI:** Koin (KMP)  
**Database:** SQLDelight + SQLCipher (encrypted)  
**UI:** Compose Multiplatform with ADHD-friendly design system  

### Key Features
- **Offline-only**: No servers, no analytics, all data local & encrypted
- **ADHD-friendly UX**: Low cognitive load, big primary actions, minimal taps, gentle feedback
- **Modular architecture**: Feature modules can be toggled on/off
- **Accessibility**: Dynamic text scaling, large tap targets, high contrast, reduce motion support
- **Privacy-first**: Encrypted database, optional app lock, no network permissions

## 🏗️ Architecture

### Module Structure

```
pocketadhd/
├── composeApp/                 # Main app with Android/iOS launchers
├── build-logic/               # Convention plugins (planned)
├── core/
│   ├── ui/                    # Design tokens (colors, typography, spacing)
│   ├── designsystem/          # ADHD-friendly components
│   ├── domain/                # Models, validators, use cases
│   ├── data/                  # Repositories, DAOs, encryption
│   ├── notifications/         # Local notifications wrapper
│   └── export/                # JSON export/import with DTOs
└── feature/
    ├── home/                  # Today's overview with quick actions
    ├── planner/               # Tasks, subtasks, reminders
    ├── focus/                 # Pomodoro timer and focus sessions
    ├── routines/              # Morning/evening/hygiene routines
    ├── mood/                  # 3-tap mood check-in with trends
    ├── meds/                  # Medication schedules (optional)
    ├── games/                 # Cognitive mini-games (optional)
    ├── tips/                  # CBT tips and breathing (optional)
    └── settings/              # Feature toggles, privacy, export
```

### Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **UI Framework** | Compose Multiplatform | Cross-platform native UI |
| **Navigation** | Decompose | Component-based navigation |
| **Architecture** | MVVM + MVI | Unidirectional state flow |
| **Database** | SQLDelight + SQLCipher | Encrypted local storage |
| **DI** | Koin | Dependency injection |
| **Serialization** | kotlinx.serialization | JSON export/import |
| **Date/Time** | kotlinx-datetime | Cross-platform date handling |
| **Settings** | Multiplatform Settings | Lightweight preferences |
| **Coroutines** | kotlinx.coroutines | Async operations |

## 🎨 Design System

### ADHD-Friendly Design Principles

1. **Generous Spacing**: Reduces visual clutter and cognitive load
2. **Large Touch Targets**: Minimum 48dp for easy interaction
3. **High Contrast Colors**: Ensures readability and accessibility
4. **Clear Visual Hierarchy**: Uses typography and spacing for organization
5. **Calming Color Palette**: Blue-green primary with warm orange accents
6. **Consistent Patterns**: Predictable layouts and interactions
7. **Haptic Feedback**: Confirms actions without overwhelming

### Design Tokens

- **Colors**: ADHD-friendly palette with mood-specific colors
- **Typography**: Larger base sizes, generous line heights, clear font weights
- **Spacing**: Component-specific spacing for buttons, cards, screens, etc.
- **Components**: Specialized ADHD-friendly buttons, cards, timers, mood scales

## 📱 Features Implementation Status

### ✅ Completed Features

#### Core Infrastructure
- [x] **Module Structure**: Complete modular architecture with proper separation
- [x] **Domain Models**: All data models (Task, Routine, FocusSession, MoodEntry, etc.)
- [x] **Database Schema**: SQLDelight schema with encrypted storage setup
- [x] **Repository Pattern**: Repository implementations for all entities
- [x] **Navigation System**: Decompose-based navigation with bottom tabs

#### Design System
- [x] **Design Tokens**: Colors, typography, spacing tokens
- [x] **ADHD Components**: Buttons, cards, timers, mood scales
- [x] **Theme System**: Light/dark themes with ADHD-friendly colors
- [x] **Accessibility**: Large touch targets, high contrast, dynamic text

#### App Structure
- [x] **Main App**: Integrated navigation with theme system
- [x] **Bottom Navigation**: 6 main sections (Home, Tasks, Focus, Routines, Mood, Settings)
- [x] **Placeholder Screens**: Basic screens for all feature modules

### 🚧 In Progress / Planned Features

#### MVP Features (Priority 1)
- [ ] **Home Screen**: "Today" card stack with next task, routine step, quick actions
- [ ] **Task Planner**: Create/edit tasks with subtasks, due dates, reminders
- [ ] **Focus Timer**: Pomodoro timer (25/5 default) with visual countdown
- [ ] **Routines**: Morning/evening/hygiene checklists with step timers
- [ ] **Mood Tracking**: 3-tap check-in (mood/focus/energy) with simple trends
- [ ] **Settings**: Module toggles, theme selection, text scaling

#### Advanced Features (Priority 2)
- [ ] **Medication Management**: Schedules, reminders, adherence tracking
- [ ] **Cognitive Games**: N-back and Go/No-Go mini-games
- [ ] **CBT Tips**: Contextual micro-tips and breathing exercises
- [ ] **Export/Import**: JSON backup with preview and merge options

#### Security & Privacy (Priority 3)
- [ ] **Encryption Keys**: Android Keystore / iOS Keychain integration
- [ ] **App Lock**: Biometric/PIN protection for app and export
- [ ] **Local Notifications**: Cross-platform notification system

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (latest stable)
- **Xcode** (for iOS development)
- **JDK 11** or higher
- **Kotlin Multiplatform Mobile plugin**

### Running the Project

#### Android
```bash
./gradlew :composeApp:assembleDebug
# Or run directly from Android Studio
```

#### iOS
```bash
cd iosApp
open iosApp.xcodeproj
# Build and run from Xcode
```

#### Build All Targets
```bash
./gradlew build
```

### Project Setup
1. Clone the repository
2. Open in Android Studio with KMP plugin installed
3. Sync Gradle dependencies
4. For iOS: Open `iosApp/iosApp.xcodeproj` in Xcode

## 📊 Database Schema

### Core Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **TaskEntity** | Tasks and subtasks | title, dueAt, estimateMinutes, isDone |
| **RoutineEntity** | Daily routines | name, steps, schedule, isActive |
| **FocusSessionEntity** | Pomodoro sessions | startAt, targetMinutes, completed |
| **MoodEntryEntity** | Mood check-ins | mood (-2..+2), focus (0..4), energy (0..4) |
| **MedicationPlanEntity** | Med schedules | name, dose, times, daysOfWeek |
| **GameResultEntity** | Game scores | gameType, score, durationSeconds |

### Encryption
- **Database**: SQLCipher encryption at rest
- **Keys**: Stored in Android Keystore / iOS Keychain
- **No Network**: Zero network permissions, fully offline

## 🎮 User Experience

### ADHD-Friendly Features
1. **3-Tap Interactions**: Quick mood check-in, minimal cognitive load
2. **Visual Timers**: Large, clear countdown displays with progress rings
3. **Gentle Notifications**: Respectful reminders without overwhelming
4. **One Task Per Screen**: Progressive disclosure, clear focus
5. **Big Primary Actions**: Prominent buttons for main actions
6. **Undo Support**: Safety net for destructive actions
7. **Empty States**: Encouraging first-time user experience

### Accessibility
- **Dynamic Text**: Supports system text scaling
- **High Contrast**: WCAG AA compliant color ratios
- **Large Targets**: Minimum 48dp touch targets
- **Screen Reader**: Proper content descriptions
- **Reduce Motion**: Respects system animation preferences

## 🔧 Development Guidelines

### Code Organization
- **Feature Modules**: Self-contained with public API
- **Clean Architecture**: Domain → Data → UI layers
- **MVVM Pattern**: ViewModels with StateFlow/Flow
- **Repository Pattern**: Abstract data access
- **Use Cases**: Pure domain logic

### Testing Strategy
- **Unit Tests**: Domain logic and repositories
- **UI Tests**: Key user flows (Android Compose)
- **Integration Tests**: Database and navigation
- **Accessibility Tests**: Screen reader and scaling

### Build Configuration
- **Version Catalog**: Centralized dependency management
- **Convention Plugins**: Shared build configuration (planned)
- **Code Quality**: Detekt/ktlint integration (planned)
- **CI/CD**: Automated testing and builds (planned)

## 📈 Roadmap

### Phase 1: MVP Foundation ✅
- [x] Core architecture and navigation
- [x] Design system and components
- [x] Database schema and repositories

### Phase 2: Core Features 🚧
- [ ] Home, Planner, Focus, Routines, Mood
- [ ] Basic settings and module toggles
- [ ] Local notifications

### Phase 3: Advanced Features
- [ ] Medication management
- [ ] Cognitive games
- [ ] CBT tips and breathing
- [ ] Export/import system

### Phase 4: Polish & Security
- [ ] App lock and encryption
- [ ] Comprehensive testing
- [ ] Performance optimization
- [ ] App store preparation

## 🤝 Contributing

This is currently a solo development project focused on creating a production-ready ADHD support app. The codebase follows clean architecture principles and is designed for maintainability and extensibility.

### Key Principles
- **Privacy First**: No data collection, fully offline
- **ADHD-Friendly**: Every design decision considers cognitive load
- **Accessibility**: Inclusive design for all users
- **Quality**: Production-ready code with proper testing

## 📄 License

This project is developed as a personal ADHD support tool with focus on privacy and accessibility. License details to be determined based on distribution strategy.

---

**Built with ❤️ for the ADHD community**  
*Helping manage attention, routines, and well-being through thoughtful technology*
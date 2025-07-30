# PocketADHD - ADHD Assistant App

A **Kotlin Multiplatform** ADHD support app designed for teens and adults, focusing on attention, routines, mood tracking, and task management. Built with offline-first architecture, encrypted local storage, and ADHD-friendly design principles.

**⚠️ CURRENT STATUS: FOUNDATION COMPLETE, UI LAYER IN DEVELOPMENT**

## 🎯 Project Overview

**Target Platforms:** Android + iOS  
**Architecture:** MVVM with unidirectional state flow  
**Navigation:** Decompose (root/child components, back stack)  
**DI:** Koin (KMP)  
**Database:** SQLDelight + SQLCipher (encrypted)  
**UI:** Compose Multiplatform with ADHD-friendly design system  
**Status:** 🚧 **Data Layer Complete, UI Layer 30% Complete**

### Key Features (Planned)
- **Offline-only**: No servers, no analytics, all data local & encrypted
- **ADHD-friendly UX**: Low cognitive load, big primary actions, minimal taps, gentle feedback
- **Modular architecture**: Feature modules can be toggled on/off
- **Accessibility**: Dynamic text scaling, large tap targets, high contrast, reduce motion support
- **Privacy-first**: Encrypted database with secure key management, no network permissions
- **Cross-platform notifications**: Local reminders for tasks and routines

## 🏗️ Architecture

### Module Structure

```
pocketadhd/
├── composeApp/                 # Main app with Android/iOS launchers ✅
├── build-logic/               # Convention plugins (planned) ❌
├── core/
│   ├── ui/                    # Design tokens (colors, typography, spacing) ✅
│   ├── designsystem/          # ADHD-friendly components ✅
│   ├── domain/                # Models, validators, use cases ✅
│   ├── data/                  # Repositories, DAOs, encryption ✅
│   ├── notifications/         # Local notifications wrapper ✅
│   └── export/                # JSON export/import with DTOs ❌
└── feature/
    ├── home/                  # Today's overview with quick actions ⚠️
    ├── planner/               # Tasks, subtasks, reminders ⚠️
    ├── focus/                 # Pomodoro timer and focus sessions ❌
    ├── routines/              # Morning/evening/hygiene routines ❌
    ├── mood/                  # 3-tap mood check-in with trends ❌
    ├── meds/                  # Medication schedules (optional) ❌
    ├── games/                 # Cognitive mini-games (optional) ❌
    ├── tips/                  # CBT tips and breathing (optional) ❌
    └── settings/              # Feature toggles, privacy, export ❌
```

**Legend:**
- ✅ **Complete**: Fully implemented and functional
- ⚠️ **Partial**: Core functionality implemented, UI interactions incomplete
- ❌ **Not Started**: Only scaffolding/interfaces exist

### Technology Stack

| Component | Technology | Version | Implementation Status | Notes |
|-----------|------------|---------|----------------------|-------|
| **UI Framework** | Compose Multiplatform | 1.8.2 | ✅ Complete | All screens have UI implementations |
| **Navigation** | Decompose | 3.2.0 | ✅ Complete | Root/child components with back stack |
| **Architecture** | MVVM + MVI | - | ⚠️ Partial | ViewModels exist, many actions are TODOs |
| **Database** | SQLDelight | 2.0.2 | ✅ Complete | All schemas and queries implemented |
| **Encryption** | SQLCipher | 4.6.1 | ⚠️ Android Only | iOS encryption not implemented |
| **DI** | Koin | 4.1.0 | ✅ Complete | Full DI container with all dependencies |
| **Security** | Android Keystore | - | ⚠️ Android Only | iOS uses UserDefaults (insecure) |
| **Notifications** | Platform Native | - | ⚠️ Partial | Implementation exists, integration incomplete |
| **Serialization** | kotlinx.serialization | 1.7.3 | ✅ Complete | JSON handling for all data models |
| **Date/Time** | kotlinx-datetime | 0.6.1 | ✅ Complete | Cross-platform date handling |
| **Coroutines** | kotlinx.coroutines | 1.9.0 | ✅ Complete | Async operations and flows |

### Architecture Implementation

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                       │
│  HomeScreen ✅ │ PlannerScreen ✅ │ FocusScreen ❌ │ etc.   │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 MVI Stores                                  │
│  HomeStore ✅ │ PlannerViewModel ✅ │ FocusStore ✅          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                  Repositories                               │
│  TaskRepository ✅ │ FocusSessionRepository ✅ │ etc. ✅     │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│              SQLDelight + SQLCipher                         │
│                 Encrypted Database ✅                       │
└─────────────────────────────────────────────────────────────┘
```

## 🎨 Design System

### ADHD-Friendly Design Principles ✅

1. **Generous Spacing**: Reduces visual clutter and cognitive load
2. **Large Touch Targets**: Minimum 48dp for easy interaction
3. **High Contrast Colors**: Ensures readability and accessibility
4. **Clear Visual Hierarchy**: Uses typography and spacing for organization
5. **Calming Color Palette**: Blue-green primary with warm orange accents
6. **Consistent Patterns**: Predictable layouts and interactions
7. **Haptic Feedback**: Confirms actions without overwhelming

### Design Tokens ✅

- **Colors**: ADHD-friendly palette with mood-specific colors
- **Typography**: Larger base sizes, generous line heights, clear font weights
- **Spacing**: Component-specific spacing for buttons, cards, screens, etc.
- **Components**: Specialized ADHD-friendly buttons, cards, timers, mood scales

## 📱 Features Implementation Status

### ✅ **FULLY COMPLETE**

#### Core Infrastructure ✅
- [x] **Module Structure**: Complete modular architecture with proper separation
- [x] **Domain Models**: All data models (Task, Routine, FocusSession, MoodEntry, Medication, GameResult)
- [x] **Database Schema**: SQLDelight schema with all entities and queries
- [x] **Repository Pattern**: Complete repository implementations for all entities
- [x] **Dependency Injection**: Complete Koin DI setup with all dependencies
- [x] **Build System**: Successful Android/iOS builds with proper configuration

#### Data Layer ✅
- [x] **Task Management**: Full CRUD operations with subtasks, tags, due dates
- [x] **Focus Sessions**: Session tracking with interruption counting
- [x] **Mood Tracking**: 3-scale mood entries with trend analysis queries
- [x] **Routines**: Routine and step management with scheduling
- [x] **Medications**: Medication plans and intake tracking
- [x] **Game Results**: Cognitive game score tracking
- [x] **Encrypted Storage**: SQLCipher database encryption (Android)
- [x] **Key Management**: Android Keystore integration

#### Design System ✅
- [x] **ADHD-Friendly Components**: AdhdButton, AdhdCard, AdhdTimer, AdhdMoodScale
- [x] **Theme System**: Colors, typography, spacing tokens
- [x] **Accessibility**: 48dp+ touch targets, high contrast support
- [x] **Responsive Design**: Proper spacing and typography scaling

#### Home Feature ✅

- [x] **HomeStore**: Complete MVI implementation with proper state management
- [x] **HomeComponent**: Full MVI component with navigation effects
- [x] **HomeScreen**: Full UI implementation with ADHD-friendly design
- [x] **Navigation Actions**: Complete navigation integration

#### Focus Feature ✅

- [x] **FocusStore**: Complete MVI implementation with proper state management
- [x] **FocusComponent**: Full MVI component with effect handling
- [x] **FocusScreen**: Full UI implementation with ADHD-friendly design
- [x] **Session Management**: Complete focus session lifecycle

### ⚠️ **PARTIALLY COMPLETE**

#### Planner Feature ⚠️
- [x] **PlannerViewModel**: Complete task management logic with filtering/sorting
- [x] **PlannerScreen**: Full UI implementation
- [x] **TaskEditorDialog**: Task creation/editing UI
- [ ] **PlannerComponent**: All methods are TODO stubs (PlannerComponent.kt:101-131)
- [ ] **Task Editor Integration**: Component-ViewModel connection missing

#### Security ⚠️
- [x] **Android Security**: Full encryption with Android Keystore
- [ ] **iOS Security**: Uses UserDefaults instead of Keychain (KeyManager.ios.kt:58,68)
- [ ] **iOS Encryption**: SQLCipher not implemented for iOS (DatabaseDriverFactory.ios.kt:12)

#### Notifications ⚠️
- [x] **Notification System**: Cross-platform implementation exists
- [x] **Task Reminders**: Integrated in TaskRepository (15min before due)
- [ ] **Notification Integration**: Some TODOs in platform implementations

### ❌ **NOT IMPLEMENTED**

#### Routines Feature ❌
- [x] **RoutineRepository**: Data layer complete
- [x] **RoutinesViewModel**: Business logic implemented
- [x] **RoutinesScreen**: UI implementation exists
- [ ] **RoutinesComponent**: All methods are TODO stubs (RoutinesComponent.kt:168-182)

#### Mood Feature ❌
- [x] **MoodEntryRepository**: Data layer complete with trend analysis
- [x] **MoodViewModel**: Business logic implemented
- [x] **MoodScreen**: UI implementation exists
- [ ] **MoodComponent**: All methods are TODO stubs (MoodComponent.kt:136-148)

#### Settings Feature ❌
- [x] **SettingsViewModel**: Business logic implemented
- [x] **SettingsScreen**: UI implementation exists
- [ ] **SettingsComponent**: All methods are TODO stubs (SettingsComponent.kt:181-189)

#### Not Started ❌
- [ ] **Medication Management**: Repository exists, no UI integration
- [ ] **Cognitive Games**: Repository exists, no UI integration
- [ ] **CBT Tips**: No implementation
- [ ] **Export/Import**: Module exists but empty
- [ ] **App Lock**: Not implemented
- [ ] **Advanced Notifications**: Basic implementation only

## 🔧 Code Quality Analysis

### ✅ **Strengths**

1. **Excellent Data Layer Architecture**
   - Clean repository pattern with proper abstraction
   - Comprehensive SQLDelight schemas with optimized queries
   - Proper domain model design with serialization
   - Robust error handling in repositories

2. **Well-Designed Domain Models**
   - Thoughtful field design (nullable where appropriate)
   - Proper relationships (Task -> Subtask, MedicationPlan -> MedicationIntake)
   - Good use of kotlinx-datetime and serialization

3. **ADHD-Friendly Design System**
   - Consistent spacing and typography tokens
   - Large touch targets and high contrast
   - Custom components designed for cognitive load reduction

4. **Modern Technology Stack**
   - Latest KMP and Compose Multiplatform versions
   - Proper coroutine usage with Dispatchers
   - Clean dependency injection with Koin

### ⚠️ **Code Smells & Issues**

1. **Massive TODO Debt (97+ items)**
   - Most feature components are just TODO stubs
   - Navigation actions in ViewModels are incomplete
   - Many UI interactions not wired up

2. **Inconsistent Implementation Status**
   - README claims "production ready" but most features are incomplete
   - Data layer is excellent but UI layer is largely non-functional
   - ViewModels exist but aren't connected to Components

3. **Security Vulnerabilities**
   - iOS KeyManager uses UserDefaults instead of Keychain
   - iOS SQLCipher encryption not implemented
   - Potential data exposure on iOS platform

4. **Architecture Gaps**
   - Components (Decompose) not properly connected to ViewModels
   - Navigation actions are TODOs in most ViewModels
   - Missing error handling in UI layer

5. **Platform Inconsistencies**
   - Android has full security implementation
   - iOS security is incomplete/insecure
   - Different notification implementations

### 🔍 **Specific Code Issues**

```kotlin
// SECURITY ISSUE - iOS KeyManager (KeyManager.ios.kt:58)
// Store in UserDefaults for now - TODO: Implement proper Keychain storage
// UserDefaults.standard.set(keyData, forKey: keyAlias)

// ARCHITECTURE ISSUE - Components not implemented (PlannerComponent.kt:102)
override val uiState: StateFlow<PlannerUiState> = TODO()

// ARCHITECTURE ISSUE - Focus Component not implemented (FocusComponent.kt:96)
override fun onStartSession(durationMinutes: Int) {
    // TODO: Implement focus session start logic
}
```

## 🚀 Current Build Status

### ✅ **What Actually Works**
- **Android Build**: Compiles successfully
- **Database Operations**: All CRUD operations functional
- **Data Flow**: Repository -> ViewModel data flow works
- **UI Rendering**: All screens render correctly
- **Design System**: Components display properly

### ❌ **What Doesn't Work**
- **Feature Navigation**: Most navigation actions are TODOs
- **User Interactions**: Button clicks often lead to TODO stubs
- **iOS Security**: Data stored insecurely in UserDefaults
- **Cross-Feature Integration**: Features don't communicate
- **Notifications**: Not properly integrated with UI

## 📊 Database Schema (Complete ✅)

### Core Entities

| Entity | Purpose | Key Fields | Status |
|--------|---------|------------|--------|
| **TaskEntity** | Tasks and subtasks | title, dueAt, estimateMinutes, isDone | ✅ Complete |
| **SubtaskEntity** | Task breakdown | taskId, title, isDone | ✅ Complete |
| **RoutineEntity** | Daily routines | name, steps, schedule, isActive | ✅ Complete |
| **RoutineStepEntity** | Routine steps | routineId, title, durationSeconds | ✅ Complete |
| **FocusSessionEntity** | Pomodoro sessions | startAt, targetMinutes, completed, interruptions | ✅ Complete |
| **MoodEntryEntity** | Mood check-ins | mood (-2..+2), focus (0..4), energy (0..4) | ✅ Complete |
| **MedicationPlanEntity** | Med schedules | name, dose, times, daysOfWeek | ✅ Complete |
| **MedicationIntakeEntity** | Med tracking | planId, timestamp, taken, sideEffects | ✅ Complete |
| **GameResultEntity** | Game scores | gameType, score, durationSeconds | ✅ Complete |

### Advanced Queries ✅
- **Mood Trends**: Average calculations and date range filtering
- **Focus Statistics**: Session completion rates and time tracking
- **Task Analytics**: Overdue detection and completion tracking
- **Routine Scheduling**: Day-of-week and time-based queries

## 🎮 User Experience (Theoretical)

### ADHD-Friendly Features (Designed)
1. **3-Tap Interactions**: Mood check-in system designed
2. **Visual Timers**: Components exist but not integrated
3. **Gentle Notifications**: System exists but not connected
4. **One Task Per Screen**: UI follows this principle
5. **Big Primary Actions**: Design system supports this
6. **Undo Support**: Not implemented
7. **Empty States**: UI components exist

### Accessibility (Partial)
- **Dynamic Text**: Design system supports it
- **High Contrast**: Color system implemented
- **Large Targets**: 48dp minimum in design system
- **Screen Reader**: Basic support, not comprehensive
- **Reduce Motion**: Planned but not implemented

## 🔧 Development Guidelines

### Code Organization ✅
- **Feature Modules**: Self-contained with clear boundaries
- **Clean Architecture**: Domain → Data → UI layers properly separated
- **MVVM Pattern**: ViewModels with StateFlow/Flow implemented
- **Repository Pattern**: Excellent abstraction over data sources
- **Use Cases**: Embedded in ViewModels (could be extracted)

### Testing Strategy ❌
- **Unit Tests**: Not implemented
- **UI Tests**: Not implemented
- **Integration Tests**: Not implemented
- **Accessibility Tests**: Not implemented

### Build Configuration ✅
- **Version Catalog**: Centralized dependency management
- **Gradle Configuration**: Proper KMP setup
- **Platform Targets**: Android and iOS configured
- **Code Quality Tools**: Not configured (Detekt, ktlint)

## 📈 Realistic Roadmap

### Phase 1: Fix Foundation Issues 🚧 **URGENT**
- [ ] **iOS Security**: Implement proper Keychain storage
- [ ] **iOS Encryption**: Add SQLCipher support for iOS
- [ ] **Component Integration**: Connect Components to ViewModels
- [ ] **Navigation Actions**: Implement all TODO navigation methods
- [ ] **Error Handling**: Add proper error handling in UI layer

### Phase 2: Complete Core Features 📋 **HIGH PRIORITY**
- [ ] **Planner Feature**: Connect PlannerComponent to PlannerViewModel
- [ ] **Mood Feature**: Connect MoodComponent to MoodViewModel
- [ ] **Settings Feature**: Implement settings persistence and UI integration

### Phase 3: Advanced Features 📋 **MEDIUM PRIORITY**
- [ ] **Routines**: Complete routine execution flow
- [ ] **Medication Management**: Build medication tracking UI
- [ ] **Notifications**: Integrate notification system with UI
- [ ] **Export/Import**: Implement data backup/restore
- [ ] **App Lock**: Add biometric/PIN protection

### Phase 4: Polish & Production 📋 **LOW PRIORITY**
- [ ] **Comprehensive Testing**: Unit, integration, and UI tests
- [ ] **Performance Optimization**: Memory and battery usage
- [ ] **Accessibility**: Complete screen reader support
- [ ] **Code Quality**: Add Detekt, ktlint, and CI/CD
- [ ] **App Store Preparation**: Icons, screenshots, store listings

## 🚨 Critical Issues to Address

### 🔴 **Security Vulnerabilities**
1. **iOS Data Exposure**: User data stored in plain text in UserDefaults
2. **iOS Encryption Missing**: Database not encrypted on iOS
3. **Key Management**: iOS keys not securely stored

### 🔴 **Architecture Problems**
1. **Broken Navigation**: 97+ TODO items prevent app functionality
2. **Component Disconnection**: UI components not connected to business logic
3. **Incomplete Features**: Most features are non-functional despite UI existing

### 🔴 **User Experience Issues**
1. **Non-functional Buttons**: Most interactions lead to TODO stubs
2. **Missing Feedback**: No error handling or loading states in many places
3. **Incomplete Flows**: Users can't complete basic tasks

## 🎯 **Actual Current Status: Foundation with Incomplete Features**

### What's Really Working ✅
- **Data Layer**: Excellent, production-ready implementation
- **Domain Models**: Well-designed and comprehensive
- **UI Components**: Beautiful, ADHD-friendly design system
- **Android Security**: Proper encryption and key management
- **Build System**: Compiles and runs on both platforms

### What's Not Working ❌
- **Feature Integration**: Most features are non-functional
- **iOS Security**: Critical security vulnerabilities
- **User Interactions**: Most buttons don't work
- **Navigation**: Users can't navigate between features
- **Data Persistence**: UI changes don't persist (components not connected)

### Realistic Assessment
This is a **well-architected foundation** with **excellent data layer design** but **incomplete feature implementation**. The README's claims of being "production ready" are misleading. The project needs significant work to connect the UI layer to the business logic layer.

**Estimated Completion**: 60-80 hours of development to reach MVP status
**Current Functional Status**: 30% complete
**Data Layer Status**: 95% complete
**UI Layer Status**: 70% complete (renders but doesn't function)
**Integration Layer Status**: 10% complete

## 🤝 Contributing

This project demonstrates excellent architectural foundations but requires significant development to complete the feature implementations. Key areas needing work:

1. **Component-ViewModel Integration**: Connect Decompose components to ViewModels
2. **iOS Security Implementation**: Replace UserDefaults with Keychain
3. **Navigation Implementation**: Replace TODO stubs with actual navigation
4. **Error Handling**: Add comprehensive error handling throughout UI
5. **Testing**: Add unit and integration tests

### Development Priorities
1. **Security First**: Fix iOS security vulnerabilities
2. **Core Features**: Complete Home, Planner, Focus, Mood features
3. **Navigation**: Implement all navigation actions
4. **Testing**: Add comprehensive test coverage
5. **Polish**: Performance optimization and accessibility

## 📄 License

This project is developed as a personal ADHD support tool with focus on privacy and accessibility. License details to be determined based on distribution strategy.

---

**Built with ❤️ for the ADHD community**  
*A solid foundation with excellent data architecture, awaiting feature completion*

## 📞 Support & Feedback

**Current Version**: Foundation 0.3 - Data Layer Complete, UI Integration Needed  
**Last Updated**: July 2025  
**Build Status**: ✅ Compiles | ⚠️ Limited Functionality | 🔴 iOS Security Issues

**Next Milestone**: Complete component-ViewModel integration for core features
**Estimated Time to MVP**: 60-80 development hours
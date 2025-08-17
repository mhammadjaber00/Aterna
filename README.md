# Aterna - Gamified Productivity App

## Project Overview

Aterna is a Kotlin Multiplatform Compose application that gamifies productivity and focus sessions through immersive RPG
mechanics. Users create fantasy heroes with different classes (Warrior, Mage) and embark on "quests" that are actually
timed focus sessions. The app combines traditional task management with rich gamification elements including:

- **Hero Progression**: Level up your character through XP and gold rewards
- **Interactive Quests**: Focus sessions become dynamic adventures with planned events (mobs, chests, quirky encounters)
- **Class System**: Choose between Warrior (gold bonuses) or Mage (XP bonuses) with different multipliers
- **Loot & Items**: Collect items with rarity levels (Common, Rare, Epic, Legendary) and various types (Weapons, Armor,
  etc.)
- **Status Effects**: Dynamic modifiers that affect rewards (e.g., curse penalties for quitting early)
- **Task Management**: Traditional productivity features alongside the gamification layer

The core concept transforms boring focus sessions into engaging fantasy adventures where staying focused means
progressing your hero and collecting rewards.

## Architecture

Aterna follows **Clean Architecture** principles with clear separation between layers:

```
┌─────────────────────────────────────────┐
│              Presentation               │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │   UI/Compose│  │  Navigation     │   │
│  │  Components │  │ (Decompose)     │   │
│  └─────────────┘  └─────────────────┘   │
├─────────────────────────────────────────┤
│                Domain                   │
│  ┌─────────┐ ┌─────────┐ ┌───────────┐  │
│  │ Models  │ │Services │ │Repositories│  │
│  │         │ │         │ │(Interfaces)│  │
│  └─────────┘ └─────────┘ └───────────┘  │
├─────────────────────────────────────────┤
│                 Data                    │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │SqlDelight   │  │ Repository      │   │
│  │Database     │  │ Implementations │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

**Key Architectural Patterns:**

- **MVI (Model-View-Intent)**: Used for state management with unidirectional data flow
- **Repository Pattern**: Clean abstraction between domain and data layers
- **Dependency Injection**: Koin for modular dependency management
- **Component-Based Navigation**: Decompose for type-safe, lifecycle-aware navigation
- **Multiplatform Architecture**: Shared business logic with platform-specific implementations

## Features

### ✅ Completed Features

#### Hero System

- **Hero Creation**: Complete class selection with animated UI effects
- **Character Progression**: XP/level system (100 XP per level), gold accumulation
- **Class Bonuses**: Warrior (+20% Gold, 1.2x XP) and Mage (+30% XP, 1.3x Gold)
- **Statistics Tracking**: Total focus minutes, daily streaks, last active date

#### Quest System (Focus Sessions)

- **Interactive Quests**: Timed focus sessions with planned events
- **Dynamic Quest Generation**: Events scale with quest duration (4-19 beats)
- **Event Types**:
    - **Mob Encounters**: Level-scaled enemies with flee mechanics
    - **Treasure Chests**: Gold rewards with major/minor variants
    - **Quirky Events**: Humorous encounters with XP rewards
    - **Trinkets**: Lore and flavor text elements
- **Quest Planning**: Deterministic randomization with proper event spacing
- **Quest Resolution**: Context-aware outcomes based on hero level and class

#### Item & Loot System

- **Item Types**: Weapons, Armor, Accessories, Consumables, Materials
- **Rarity System**: Common (60%), Rare (25%), Epic (12%), Legendary (3%)
- **Loot Tables**: Weighted item generation system
- **Item Properties**: Value, stackability, sell mechanics (60% of base value)

#### Status Effects System

- **Curse Mechanics**: Penalties for early quest abandonment (-50% rewards)
- **Timed Effects**: Expiration-based status effect management
- **Effect Modifiers**: Dynamic reward calculation adjustments

#### Data Persistence

- **SqlDelight Database**: Cross-platform SQLite with type-safe queries
- **Encrypted Storage**: SQLCipher on Android for data security
- **Repository Layer**: Clean data access abstractions
- **Settings Management**: Multiplatform settings persistence

#### UI & Visual Design

- **Fantasy Theming**: Rich visual effects with bloom, aura rings, orbiting sparks
- **Class-Specific Palettes**: Dynamic theming based on character class
- **Animation System**: Comprehensive animation states and transitions
- **Design System**: Reusable components with consistent styling

### 🚧 Work in Progress

#### Task Management

- **Core Task System**: Basic CRUD operations implemented
- **Task Features**: Subtasks, due dates, time estimates, tagging
- **UI Integration**: Task management screens not yet connected to main flow

#### Onboarding Flow

- **Visual Components**: Sophisticated particle effects and animations completed
- **Integration**: Onboarding components exist but integration with main flow unclear

#### Notifications

- **Platform Implementations**: iOS and Android notification systems implemented
- **Quest Notifications**: Framework exists but integration status unclear

### ❌ Missing/Stubbed Features

#### ViewModels & State Management

- **Empty ViewModels Module**: TODO comment indicates ViewModels not implemented
- **State Management**: MVI base classes exist but concrete implementations missing

#### Inventory System

- **Item Storage**: No inventory management UI or business logic
- **Equipment System**: Items defined but no equipment/stats mechanics

#### Crafting System

- **Materials Usage**: Material items exist but no crafting implementation
- **Recipe System**: No crafting recipes or mechanics

#### Multiplayer/Social Features

- **Server Validation**: Quest entities have serverValidated flags but no server integration
- **Social Features**: No friend systems, leaderboards, or sharing

#### Advanced Features

- **Focus Session Integration**: FocusSession entities exist but unclear how they relate to quests
- **Daily Challenges**: No daily quest or challenge systems
- **Achievement System**: No achievement tracking or badges

## Data Layer

### Database Schema (SqlDelight)

The app uses SqlDelight for type-safe, cross-platform database access:

#### Core Tables

- **HeroEntity**: Character data with progression stats
- **QuestLogEntity**: Quest history with completion states and rewards
- **QuestEvents**: Individual quest event records
- **TaskEntity**: Task management with subtasks and metadata
- **StatusEffectEntity**: Temporary effect modifiers
- **FocusSessionEntity**: Focus session tracking

#### Key Relationships

- Heroes have many Quests (one-to-many)
- Quests have many QuestEvents (one-to-many)
- Tasks support hierarchical subtasks
- Status effects are hero-scoped

### Repository Pattern

Each domain entity has a corresponding repository:

```kotlin
// Domain Interface
interface HeroRepository {
    fun getHero(): Flow<Hero?>
    suspend fun insertHero(hero: Hero)
    suspend fun updateHeroStats(heroId: String, level: Int, xp: Int, gold: Int)
}

// Data Implementation
class HeroRepositoryImpl(database: AternaDatabase) : HeroRepository {
    // SqlDelight query execution with domain mapping
}
```

### Data Flow

1. **UI Components** → Repository interfaces (via DI)
2. **Repository Implementations** → SqlDelight queries
3. **Database Entities** ↔ Domain Models (mapping functions)
4. **Flow-based Updates** → Reactive UI updates

## Presentation Layer

### UI Structure

The presentation layer uses Jetpack Compose with a component-based architecture:

#### Navigation (Decompose)

- **AppRootComponent**: Main navigation coordinator
- **Feature Components**: Modular navigation for each feature
- **Configuration**: Type-safe routing definitions

#### Screen Architecture

- **Feature-Based Organization**: Each feature in its own module
- **Component Composition**: Reusable UI components
- **State Management**: MVI pattern with unidirectional data flow

#### Major Screens

- **ClassSelectionScreen**: Hero creation with immersive animations
- **TimerScreen**: Quest duration selection with ritual ring UI
- **Quest Components**: Event dialogs, headers, utility components

#### Design System

- **AternaTheme**: Custom theme with dark/light mode support
- **Color Palettes**: Class-specific color schemes
- **Typography**: Custom typography scale
- **Components**: Buttons, cards, animated elements

### Visual Effects System

Sophisticated animation and effects system:

- **Particle Effects**: Magical particles, star fields, comet trails
- **Animation States**: Complex multi-property animations
- **Shader Effects**: Bloom, shimmer, aura rendering
- **Haptic Feedback**: Platform-appropriate tactile responses

## Domain Layer

### Core Models

#### Hero System

```kotlin
data class Hero(
    val id: String,
    val name: String,
    val classType: ClassType,
    val level: Int,
    val xp: Int,
    val gold: Int,
    val totalFocusMinutes: Int,
    val dailyStreak: Int
)

enum class ClassType(
    val xpMultiplier: Double,
    val goldMultiplier: Double
)
```

#### Quest System

```kotlin
data class Quest(
    val durationMinutes: Int,
    val startTime: Instant,
    val completed: Boolean,
    val gaveUp: Boolean
)

data class PlannedEvent(
    val type: EventType, // MOB, CHEST, QUIRKY, TRINKET
    val dueAt: Instant,
    val isMajor: Boolean
)
```

### Business Logic Services

#### QuestPlanner

- **Event Generation**: Creates timed events based on quest duration
- **Scaling Logic**: More events for longer quests (4-19 beats)
- **Event Distribution**: Major/minor event balancing
- **Randomization**: Deterministic seeded random generation

#### QuestResolver

- **Event Resolution**: Converts planned events to concrete outcomes
- **Hero Context**: Considers hero level and class for outcomes
- **Reward Calculation**: XP/gold based on event type and difficulty
- **Narrative Generation**: Creates immersive event descriptions

#### RewardService

- **Modifier Application**: Applies status effects to base rewards
- **Curse Mechanics**: Penalty system for early quest abandonment
- **Class Bonuses**: Applies class-specific reward multipliers

### Utilities & Helpers

#### Core Utilities

- **TimeProvider**: Testable time abstraction
- **LootRoller**: Weighted random item generation
- **RewardBanking**: Reward accumulation and distribution
- **QuestStrings**: Narrative text and flavor content

#### Platform Abstractions

- **Notification Services**: Cross-platform notification handling
- **Database Drivers**: Platform-specific SqlDelight drivers
- **Settings**: Multiplatform preferences management

## Utilities & Helpers

### Dependency Injection (Koin)

Modular DI setup with feature-specific modules:

```kotlin
val commonModules = listOf(
    dataModule,        // Database, repositories
    domainModule,      // Services, business logic  
    notificationsModule, // Platform notifications
    focusModule,       // Quest system
    onboardingModule   // User onboarding
)
```

### Platform-Specific Implementations

#### iOS

- **MainViewController**: SwiftUI integration point
- **QuestNotifierIos**: iOS-specific notifications
- **DatabaseDriverFactory.ios**: Native SQLite driver

#### Android

- **MainActivity**: Android entry point
- **QuestActions**: Android notification actions
- **DatabaseDriverFactory.android**: Android SQLite driver

### Error Handling

- **AppError**: Centralized error definitions
- **Result Types**: Functional error handling patterns
- **Validation**: Input validation and business rule enforcement

## Current Status

### What's Working ✅

1. **Complete Hero System**: Character creation, progression, class bonuses
2. **Interactive Quest Generation**: Sophisticated event planning and resolution
3. **Rich Visual Design**: Fantasy theming with advanced animations
4. **Database Layer**: Full persistence with SqlDelight
5. **Platform Abstractions**: iOS/Android specific implementations
6. **Core Architecture**: Clean architecture with proper separation

### What's Stubbed/Incomplete ⚠️

1. **ViewModels**: Module exists but empty (major gap)
2. **Task Management UI**: Data models exist but no UI integration
3. **Inventory System**: Items defined but no management UI
4. **Notifications Integration**: Platform code exists but unclear integration
5. **Onboarding Flow**: Components exist but integration unclear

### What's Missing ❌

1. **State Management**: No concrete MVI implementations
2. **Main App Navigation**: Unclear how features connect
3. **Crafting System**: No crafting mechanics despite material items
4. **Multiplayer Features**: Server validation flags but no backend
5. **Advanced Gamification**: No achievements, daily challenges, etc.

## How to Run

### Prerequisites

- **JDK 11+** for Kotlin compilation
- **Android Studio** for Android development
- **Xcode 14+** for iOS development (macOS only)

### Setup Steps

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd aterna
   ```

2. **Install Dependencies**
   ```bash
   ./gradlew build
   ```

3. **Run Android**
   ```bash
   ./gradlew :composeApp:installDebug
   # or open in Android Studio
   ```

4. **Run iOS** (macOS only)
   ```bash
   cd iosApp
   open iosApp.xcodeproj
   # Build and run in Xcode
   ```

### Development Setup

1. **Database Migrations**
    - SqlDelight generates code automatically
    - Schema files in `src/commonMain/sqldelight/`

2. **Platform Testing**
    - Android: Use emulator or physical device
    - iOS: Use Simulator or physical device

3. **Common Code Testing**
   ```bash
   ./gradlew :composeApp:testDebugUnitTest
   ```

## Next Steps

### Immediate Priorities (Critical for MVP)

1. **Implement ViewModels & State Management**
    - Create concrete MVI ViewModels for each screen
    - Implement state management for hero progression
    - Connect quest system to UI state

2. **Complete Main Navigation Flow**
    - Connect onboarding → hero creation → main app
    - Implement quest start/completion flow
    - Add task management screen integration

3. **Fix Missing UI Connections**
    - Connect TimerScreen to actual quest creation
    - Implement quest progress/completion screens
    - Add inventory/hero stats displays

### Secondary Features

4. **Enhance Task Management**
    - Build task management UI screens
    - Connect tasks to quest system (task completion during quests)
    - Implement task-based reward bonuses

5. **Complete Notification System**
    - Integrate platform notifications with quest events
    - Add quest reminders and progress updates
    - Implement focus session break notifications

6. **Expand Gamification**
    - Build inventory management UI
    - Implement basic crafting system
    - Add achievement/badge system
    - Create daily challenge mechanics

### Long-term Goals

7. **Advanced Features**
    - Equipment system with stat bonuses
    - Guild/social features
    - Server backend for validation/sync
    - Advanced quest types and storylines

### Technical Debt

8. **Code Quality Improvements**
    - Add comprehensive test coverage
    - Implement error handling throughout
    - Add logging and analytics
    - Performance optimization for animations

---

## Getting Started as a New Developer

1. **Start with Domain Layer**: Understand the core business logic in `domain/`
2. **Examine Database Schema**: Review `.sq` files to understand data structure
3. **Study Feature Components**: Look at `features/` to understand UI patterns
4. **Focus on Quest System**: This is the core differentiator - understand QuestPlanner/Resolver
5. **Identify Integration Points**: The ViewModels gap is the main blocker for connecting everything

The project has solid foundations but needs the presentation layer completed to become a functional app. The
gamification concept is unique and well-designed - focus on connecting the rich domain logic to the beautiful UI
components that already exist.
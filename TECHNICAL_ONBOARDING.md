# Aterna - Extended Technical Onboarding Guide

## Dependency Graph

### Module Architecture Overview

The project follows a clean dependency flow from features down to data layer:

```
┌─────────────────────────────────────────────────────────────┐
│                    PLATFORM LAYERS                         │
│  ┌──────────────────┐              ┌─────────────────────┐  │
│  │   Android Main   │              │    iOS Main        │  │
│  │  - MainActivity  │              │ - MainViewController│  │
│  │  - Notifications │              │ - Notifications     │  │
│  │  - Database      │              │ - Database          │  │
│  └──────────────────┘              └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     FEATURE MODULES                        │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │  onboardingModule│  │   focusModule   │  │notifications│  │
│  │                 │  │  (QuestModule)  │  │   Module    │  │
│  │ - OnboardingStore│  │  - QuestStore   │  │ - Notifiers │  │
│  │   (MISSING!)    │  │    (MISSING!)   │  │             │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
│              │                 │                    │       │
│              └─────────────────┼────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                          │
│                                                             │
│  ┌──────────────────┐  ┌─────────────────┐ ┌─────────────┐  │
│  │     Services     │  │   Utilities     │ │ Repositories│  │
│  │ - RewardService  │  │ - QuestPlanner  │ │ (Interfaces)│  │
│  │ - TaskNotification│  │ - QuestResolver │ │ - Hero      │  │
│  │   Service        │  │ - RewardBanking │ │ - Quest     │  │
│  └──────────────────┘  └─────────────────┘ │ - Task      │  │
│                                            │ - Settings  │  │
│                                            │ - StatusFx  │  │
│                                            └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                           │
│                                                             │
│  ┌─────────────────┐  ┌──────────────────┐ ┌─────────────┐  │
│  │   dataModule    │  │   SqlDelight     │ │ Repository  │  │
│  │                 │  │   Database       │ │ Impls       │  │
│  │ - AternaDatabase│  │ - HeroEntity     │ │ - HeroRepo  │  │
│  │ - Settings      │  │ - QuestLogEntity │ │ - QuestRepo │  │
│  │ - TimeProvider  │  │ - TaskEntity     │ │ - TaskRepo  │  │
│  │ - Mock QuestApi │  │ - StatusEffectEntity│           │  │
│  └─────────────────┘  └──────────────────┘ └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### DI Module Dependencies

**Key Relationships:**

- `focusModule` → depends on 6 domain services (heroRepository, questRepository, questNotifier, etc.)
- `onboardingModule` → depends only on settingsRepository (lightweight)
- `dataModule` → provides all repositories and core services
- `notificationsModule` → platform-specific notification implementations
- Platform modules → provide DatabaseDriverFactory for each platform

## File/Class Index

### 🔥 Critical Files (Must Understand)

| File/Class                   | Purpose                       | Notes                                                        |
|------------------------------|-------------------------------|--------------------------------------------------------------|
| `CommonKoinModule.kt`        | **Main DI coordinator**       |                                                              |
| `AppRootComponent.kt`        | **Navigation hub**            | Defines 4-screen flow: Onboarding→ClassSelect→QuestHub→Timer |
| `DefaultAppRootComponent.kt` | **Navigation implementation** | Actual routing logic and state management                    |
| `QuestPlanner.kt`            | **Core quest logic**          | Generates timed events (4-19 beats) based on duration        |
| `QuestResolver.kt`           | **Event resolution**          | Converts planned events to concrete outcomes with rewards    |
| `RewardService.kt`           | **Reward calculation**        | Applies status effects and class bonuses to base rewards     |

### 🏗️ Domain Models (Core Business Logic)

| File/Class                                  | Purpose                 | Key Properties                                       |
|---------------------------------------------|-------------------------|------------------------------------------------------|
| `Hero.kt`                                   | **Player character**    | level, xp, gold, classType, totalFocusMinutes        |
| `Quest.kt`                                  | **Focus session**       | durationMinutes, startTime, completed, gaveUp        |
| `Task.kt`                                   | **Task management**     | title, dueAt, estimateMinutes, subtasks, tags        |
| `ClassType.kt`                              | **Character classes**   | Warrior (1.2x XP), Mage (1.3x Gold) with multipliers |
| `Item.kt` + `ItemType.kt` + `ItemRarity.kt` | **Loot system**         | Weapons/Armor/etc with Common→Legendary rarity       |
| `StatusEffect.kt`                           | **Temporary modifiers** | CURSE_EARLY_EXIT reduces rewards by 50%              |

### 🎨 UI Components (Presentation Layer)

| File/Class                           | Purpose                   | Complexity                                                 |
|--------------------------------------|---------------------------|------------------------------------------------------------|
| `ClassSelectionScreen.kt`            | **Hero creation**         | 607 lines - sophisticated animations, class selection      |
| `TimerScreen.kt`                     | **Quest duration picker** | 221 lines - ritual ring UI, haptic feedback                |
| `RitualRing.kt`                      | **Animated timer UI**     | 454 lines - complex animation system with orbiting effects |
| `QuestRingComponents.kt`             | **Quest progress rings**  | 533 lines - dynamic progress visualization                 |
| `PixelHeroAvatar.kt`                 | **Character display**     | Pixel art hero rendering                                   |
| `BasicCards.kt` + `ButtonHelpers.kt` | **Design system**         | Reusable UI components                                     |

### 🗃️ Data Layer (Persistence)

| File/Class                | Purpose              | Key Operations                                    |
|---------------------------|----------------------|---------------------------------------------------|
| `DataModule.kt`           | **Data DI setup**    | Provides database, repositories, mock API         |
| `HeroRepositoryImpl.kt`   | **Hero persistence** | CRUD ops, stats updates, domain mapping           |
| `QuestRepositoryImpl.kt`  | **Quest history**    | Quest logging, completion tracking, event storage |
| `TaskRepositoryImpl.kt`   | **Task management**  | Task CRUD, due date queries, subtask handling     |
| `Hero.sq` + `QuestLog.sq` | **Database schema**  | SqlDelight table definitions and queries          |
| `AternaDatabase`          | **Generated DB**     | Type-safe database access from SqlDelight         |

### 🔧 Utilities & Infrastructure

| File/Class                        | Purpose                    | Role                                          |
|-----------------------------------|----------------------------|-----------------------------------------------|
| `QuestStrings.kt`                 | **Game content**           | Mob names, chest messages, quest flavor text  |
| `LootRoller.kt`                   | **RNG system**             | Weighted random item generation               |
| `RewardBanking.kt`                | **Reward accumulation**    | Strategies for distributing rewards over time |
| `LocalNotifier.ios.kt/android.kt` | **Platform notifications** | Quest reminders, progress updates             |
| `DatabaseDriverFactory`           | **DB drivers**             | Platform-specific SqlDelight driver creation  |

## Gotchas & Pain Points

### 🚨 Critical Issues (Will Break App)

1. **Missing Store Classes**
    - `QuestStore` and `OnboardingStore` referenced in DI modules **DO NOT EXIST**
    - App will crash at runtime when Koin tries to inject these classes
    - Location: `QuestModule.kt:11`, `OnboardingModule.kt:12`

2. **Empty ViewModels Module**
    - No state management layer connecting UI to domain logic
    - Location: `CommonKoinModule.kt:19`

3. **Navigation Component Mismatch**
    - Navigation expects `OnboardingRootComponent`, `ClassSelectComponent`, `QuestComponent`
    - Unclear if these components actually exist or are properly implemented

### ⚠️ Fragile Areas

4. **Mock API Integration**
    - `createMockQuestApi()` used in production DI setup
    - No real backend integration, server validation flags unused
    - Location: `DataModule.kt:53`

5. **Platform Notification Integration**
    - Complex notification system exists but unclear how it connects to quest events
    - iOS and Android implementations present but integration unclear

6. **Incomplete Task System**
    - Task domain models and repositories exist
    - UI for task management missing or not connected to main flow

### 🔧 DI Setup Quirks

7. **Module Naming Inconsistency**
    - Quest module named `focusModule` instead of `questModule`
    - Can be confusing when tracing dependencies

8. **Circular Dependency Risk**
    - `TaskRepository` depends on `TaskNotificationService`
    - Complex injection graph in `DataModule.kt:32-36`

9. **Scope Management**
    - Stores use `SupervisorJob() + Dispatchers.Main.immediate`
    - Custom coroutine scope setup might conflict with UI lifecycle

### 🧩 Missing Integrations

10. **Inventory System**
    - Item models exist but no inventory UI or management logic
    - Equipment system defined but not connected to hero stats

11. **Crafting Mechanics**
    - Material items exist but no crafting implementation
    - Recipe system missing entirely

12. **Achievement System**
    - No achievement tracking despite rich progression mechanics
    - Daily challenges, badges, milestones all missing

## Integration Map

### Intended User Flow

```
📱 App Launch
    ↓
🌟 ONBOARDING (First-time setup)
    ├─ Welcome screens & tutorials  
    ├─ Settings configuration
    └─ → Navigate to Class Selection
    ↓
⚔️  CLASS SELECTION (Hero Creation)
    ├─ Choose Warrior (Gold bonus) or Mage (XP bonus)
    ├─ Character name input
    ├─ Stats preview with class bonuses
    └─ → Create hero & navigate to Quest Hub
    ↓  
🏛️  QUEST HUB (Main Dashboard) 
    ├─ Hero stats display (level, XP, gold)
    ├─ Quest history & achievements
    ├─ Task management interface (MISSING UI)
    ├─ Inventory management (MISSING UI)
    └─ → Start new quest (navigate to Timer)
    ↓
⏰ TIMER SELECTION (Quest Setup)
    ├─ Duration picker with ritual ring UI
    ├─ Quest preview with planned events
    ├─ Class bonus indicators
    └─ → Start quest with chosen duration
    ↓
🗡️  ACTIVE QUEST (Focus Session)
    ├─ Real-time progress tracking
    ├─ Timed event notifications (mobs, chests, etc.)
    ├─ Dynamic reward calculation
    ├─ Early exit option (curse penalty)
    └─ → Complete quest OR Give up
    ↓
🎁 QUEST COMPLETION (Rewards & Progress)
    ├─ XP & gold gained display
    ├─ Loot items received
    ├─ Hero level up animations
    ├─ Quest log entry created
    └─ → Return to Quest Hub
```

### Current Implementation Status

| Flow Stage          | Status     | Issues                                         |
|---------------------|------------|------------------------------------------------|
| **App Launch**      | ✅ Working  | Proper platform setup, DI initialization       |
| **Onboarding**      | ⚠️ Partial | Components exist but integration unclear       |
| **Class Selection** | ✅ Complete | Sophisticated UI, full class system            |
| **Quest Hub**       | ❌ Missing  | Navigation exists but UI not implemented       |
| **Timer Selection** | ✅ Complete | Beautiful ritual ring UI, duration picker      |
| **Active Quest**    | ⚠️ Stubbed | Planning/resolution logic complete, UI missing |
| **Completion**      | ❌ Missing  | Reward calculation works, no completion UI     |

### Integration Gaps

#### 🔍 Missing Connections

1. **Onboarding → Class Selection**
    - OnboardingStore missing, unclear how onboarding completes
    - No settings persistence integration visible

2. **Class Selection → Quest Hub**
    - Hero creation works but Quest Hub UI doesn't exist
    - Navigation goes to `QuestComponent` but implementation unclear

3. **Quest Hub → Everything**
    - Central hub missing means no task management integration
    - No inventory system connection
    - Hero stats display not implemented

4. **Timer → Active Quest**
    - Timer picks duration but quest execution UI missing
    - Event notification system not connected to UI

5. **Quest System → Task System**
    - Both systems exist independently
    - No integration for completing tasks during quests
    - Task-based reward bonuses not implemented

#### 🔗 Intended Connections (Design Intent)

1. **Task Integration**
   ```
   Quest Start → Show related tasks
   Focus Session → Mark tasks complete 
   Quest End → Task completion bonuses
   ```

2. **Notification Flow**
   ```  
   Quest Events → Platform notifications
   Progress Updates → Background alerts
   Completion → Reward notifications
   ```

3. **Progression Systems**
   ```
   Quest Completion → Hero XP/Gold
   Loot Drops → Inventory Management  
   Level Ups → New abilities/bonuses
   Daily Streaks → Achievement unlocks
   ```

### Key Architecture Insights

- **Domain Logic is Solid**: Quest planning, resolution, and reward systems are well-designed
- **UI Components Exist**: Beautiful, sophisticated components are implemented
- **State Management Missing**: No ViewModels or Stores to connect domain to UI
- **Navigation Framework Ready**: Clean navigation structure waiting for screens
- **Data Layer Complete**: Full persistence layer with proper repository pattern

**The main blocker is the missing state management layer - implementing ViewModels/Stores would unlock most of the
existing functionality.**

---

## Summary for New Developers

### What Works Well ✅

- Rich domain modeling with quest events, hero progression, loot systems
- Beautiful UI components with sophisticated animations
- Clean architecture with proper separation of concerns
- Cross-platform database with SqlDelight
- Comprehensive DI setup with Koin

### Critical Gaps 🚨

- **Missing state management** (ViewModels/Stores don't exist)
- **Incomplete navigation** (screens referenced but not implemented)
- **No main quest flow** (planning exists, execution UI missing)

### Quick Start Priorities 🎯

1. **Implement missing Store classes** to fix DI injection failures
2. **Create ViewModels** to connect domain logic to UI components
3. **Build Quest Hub screen** as the central navigation point
4. **Connect Timer to actual quest execution** with event UI

The project has excellent foundations but needs the presentation layer completed to become functional. Focus on state
management first - it's the keystone that will unlock all the existing rich functionality.
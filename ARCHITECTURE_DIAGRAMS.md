# Aterna - Architecture Diagrams

## 1. Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            PLATFORM LAYER                                  │
│  ┌──────────────────────────┐         ┌─────────────────────────────────┐  │
│  │        Android           │         │             iOS                 │  │
│  │  ├─ MainActivity         │         │  ├─ MainViewController          │  │
│  │  ├─ QuestNotifier        │         │  ├─ QuestNotifierIos           │  │
│  │  ├─ DatabaseDriver       │         │  ├─ DatabaseDriverFactory      │  │
│  │  └─ NotificationActions  │         │  └─ LocalNotifier              │  │
│  └──────────────────────────┘         └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                            FEATURE LAYER                                   │
│  ┌───────────────┐  ┌──────────────┐  ┌─────────────┐  ┌────────────────┐  │
│  │  Onboarding   │  │    Quest     │  │   Timer     │  │ Class Selection │  │
│  │               │  │              │  │             │  │                │  │
│  │ ├─ Components │  │ ├─ QuestStore │  │ ├─ Screen   │  │ ├─ Screen       │  │
│  │ ├─ Screens    │  │ ├─ Components │  │ ├─ Ring UI  │  │ ├─ Animations   │  │
│  │ └─ Store      │  │ └─ Events     │  │ └─ Effects  │  │ └─ Hero Create  │  │
│  └───────────────┘  └──────────────┘  └─────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN LAYER                                    │
│  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────────────┐   │
│  │    Services      │  │   Utilities     │  │     Repositories         │   │
│  │                  │  │                 │  │     (Interfaces)         │   │
│  │ ├─ RewardService │  │ ├─ QuestPlanner │  │ ├─ HeroRepository        │   │
│  │ ├─ TaskNotify    │  │ ├─ QuestResolver│  │ ├─ QuestRepository       │   │
│  │ └─ StatusFx      │  │ ├─ LootRoller   │  │ ├─ TaskRepository        │   │
│  │                  │  │ ├─ RewardBanking│  │ ├─ SettingsRepository    │   │
│  │                  │  │ └─ TimeProvider │  │ └─ StatusEffectRepository│   │
│  └──────────────────┘  └─────────────────┘  └──────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        Domain Models                               │   │
│  │  Hero | Quest | Task | Item | ClassType | StatusEffect | Loot     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                             DATA LAYER                                     │
│  ┌────────────────────┐  ┌──────────────────────┐  ┌─────────────────────┐ │
│  │  Repository Impls  │  │    SqlDelight DB     │  │   External APIs     │ │
│  │                    │  │                      │  │                     │ │
│  │ ├─ HeroRepoImpl    │  │ ├─ AternaDatabase    │  │ ├─ QuestApi (Mock)  │ │
│  │ ├─ QuestRepoImpl   │  │ ├─ HeroEntity        │  │ └─ Settings API     │ │
│  │ ├─ TaskRepoImpl    │  │ ├─ QuestLogEntity    │  │                     │ │
│  │ ├─ StatusFxImpl    │  │ ├─ TaskEntity        │  │                     │ │
│  │ └─ SettingsImpl    │  │ └─ StatusEffectEntity│  │                     │ │
│  └────────────────────┘  └──────────────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Key Principles:**

- **Dependency Direction**: Always points downward (Platform → Feature → Domain → Data)
- **Clean Architecture**: Each layer only knows about layers below it
- **Separation of Concerns**: UI logic, business logic, and data access are isolated
- **Platform Independence**: Domain and Data layers are shared across platforms

---

## 2. User Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          ATERNA USER JOURNEY                               │
└─────────────────────────────────────────────────────────────────────────────┘

📱 App Launch
    │
    ├─ First Time User? ──[YES]──→ 🌟 ONBOARDING
    │                              │
    └─ Returning User ────[NO]───→ │ ├─ Welcome Tutorial
                                  │ ├─ Feature Overview  
                                  │ ├─ Settings Setup
                                  │ └─ Privacy/Permissions
                                  │
                                  ↓
                              ⚔️ CLASS SELECTION
                                  │
                                  ├─ Choose Class:
                                  │   ├─ 🛡️ Warrior (+20% Gold, 1.2x XP)
                                  │   └─ 🔮 Mage (+30% XP, 1.3x Gold)
                                  │
                                  ├─ Character Creation:
                                  │   ├─ Name Input
                                  │   ├─ Stats Preview
                                  │   └─ Bonus Explanation
                                  │
                                  └─ Create Hero
                                  │
                                  ↓
                              🏛️ QUEST HUB (Main Dashboard)
                              │
                              ├─ Hero Stats Display:
                              │   ├─ Level & XP Progress
                              │   ├─ Gold Balance
                              │   ├─ Daily Streak
                              │   └─ Total Focus Time
                              │
                              ├─ Quick Actions:
                              │   ├─ 🗡️ Start New Quest
                              │   ├─ 📋 Manage Tasks (WIP)
                              │   ├─ 🎒 View Inventory (MISSING)
                              │   └─ 📊 Quest History
                              │
                              └─ Select "Start New Quest"
                              │
                              ↓
                          ⏰ TIMER SELECTION (Quest Setup)
                              │
                              ├─ Duration Selection:
                              │   ├─ 🔥 Short (5-15 min) → 4 events
                              │   ├─ ⚡ Medium (25-45 min) → 7 events  
                              │   ├─ 🌟 Long (60-90 min) → 10 events
                              │   └─ 🏆 Epic (120+ min) → 15-19 events
                              │
                              ├─ Quest Preview:
                              │   ├─ Planned Events Summary
                              │   ├─ Expected Rewards
                              │   └─ Class Bonus Display
                              │
                              └─ Confirm & Start Quest
                              │
                              ↓
                          🗡️ ACTIVE QUEST (Focus Session)
                              │
                              ├─ Real-time Progress:
                              │   ├─ ⏱️ Timer Countdown
                              │   ├─ 📊 Progress Ring
                              │   ├─ 🎯 Next Event Preview
                              │   └─ 💰 Current Rewards
                              │
                              ├─ Timed Events:
                              │   ├─ 👹 Mob Encounters
                              │   │   ├─ Fight (gain XP + gold)
                              │   │   └─ Flee (small XP, no gold)
                              │   │
                              │   ├─ 📦 Treasure Chests
                              │   │   ├─ Regular (5-20 gold)
                              │   │   └─ Rich (major events)
                              │   │
                              │   ├─ 🎲 Quirky Events  
                              │   │   └─ Flavor text + XP
                              │   │
                              │   └─ 💎 Trinkets
                              │       └─ Lore & story elements
                              │
                              ├─ Player Options:
                              │   ├─ Continue Focus
                              │   └─ Give Up Early
                              │       └─ ⚠️ Curse Applied (-50% rewards)
                              │
                              └─ Quest Completion Trigger
                              │
                              ↓
                          🎁 QUEST COMPLETION (Rewards & Progress)
                              │
                              ├─ Rewards Summary:
                              │   ├─ 💰 Gold Earned
                              │   ├─ ⭐ XP Gained  
                              │   ├─ 🎒 Items Looted
                              │   └─ 📈 Level Progress
                              │
                              ├─ Achievements:
                              │   ├─ 🔥 Streak Bonuses
                              │   ├─ 🏆 Milestones Reached
                              │   └─ 🎯 Focus Time Goals
                              │
                              ├─ Hero Progression:
                              │   ├─ Level Up Animation
                              │   ├─ Stats Updates
                              │   └─ New Abilities Unlocked
                              │
                              └─ Quest Log Entry Created
                              │
                              ↓
                          Return to 🏛️ QUEST HUB
                              │
                              └─ Cycle continues...

┌─────────────────────────────────────────────────────────────────────────────┐
│                        PARALLEL FLOWS (Future)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📋 Task Management Flow:                                                   │
│    Quest Hub → Task List → Task Details → Quest Integration                │
│                                                                             │
│  🎒 Inventory Management:                                                   │
│    Quest Hub → Inventory → Equipment → Stats/Bonuses                       │
│                                                                             │
│  🏭 Crafting System:                                                        │
│    Inventory → Materials → Recipes → Craft Items → Equipment               │
│                                                                             │
│  📊 Analytics & Progress:                                                   │
│    Quest Hub → Statistics → Analytics → Goal Setting                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Dependency Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        COMPONENT DEPENDENCIES                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                            UI LAYER                                        │
│                                                                             │
│  📱 App                🎨 ClassSelectionScreen        ⏰ TimerScreen         │
│     │                         │                           │                 │
│     ↓                         ↓                           ↓                 │
│  🧭 AppRootComponent ────→ 🏛️ QuestComponent ────────→ 🗡️ QuestStore       │
│     │                         │                           │                 │
│     └─────────────────────────┼───────────────────────────┤                 │
│                               │                           │                 │
│  🌟 OnboardingComponent       │                           │                 │
│     │                         │                           │                 │
│     ↓                         │                           │                 │
│  📝 OnboardingStore           │                           │                 │
│     │                         │                           │                 │
│     └─────────────────────────┼───────────────────────────┘                 │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         STATE MANAGEMENT                                   │
│                                                                             │
│  🗡️ QuestStore ────────────┬─────────────────────────────────┐              │
│     │                     │                                 │              │
│     ├─ startQuest()       ├─ completeQuest()               ├─ giveUp()     │
│     ├─ trackProgress()    ├─ applyRewards()                └─ applyCurse()  │
│     └─ generateEvents()   └─ updateHero()                                   │
│     │                                                                       │
│     ↓                                                                       │
│  📝 OnboardingStore ───────┬─────────────────────────────────┐              │
│     │                     │                                 │              │
│     ├─ setFirstTime()     ├─ saveSettings()                 ├─ complete()  │
│     └─ trackProgress()    └─ validateSetup()                └─ navigate()   │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                        BUSINESS LOGIC                                      │
│                                                                             │
│  🔧 QuestPlanner ──────────┬──── 🎯 QuestResolver ──────┬──── 💰 RewardService │
│     │                     │        │                   │        │           │
│     ├─ plan()             │        ├─ resolve()        │        ├─ apply()  │
│     ├─ generateEvents()   │        ├─ calculateRewards()│        └─ modify() │
│     └─ scheduleBeats()    │        └─ createOutcome()  │                    │
│     │                     │        │                   │                    │
│     │                     │        ↓                   ↓                    │
│  🎲 LootRoller            │     📊 RewardBanking ──────────── 🔄 TimeProvider │
│     │                     │        │                                        │
│     ├─ rollItem()         │        ├─ accumulate()                          │
│     ├─ calculateRarity()  │        └─ distribute()                          │
│     └─ applyBonuses()     │                                                 │
│     │                     │                                                 │
│     │                     ↓                                                 │
│  📋 TaskNotificationService                                                 │
│     │                                                                       │
│     ├─ scheduleReminders()                                                  │
│     ├─ sendProgressUpdates()                                                │
│     └─ handleCompletion()                                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                       REPOSITORY LAYER                                     │
│                                                                             │
│  👤 HeroRepository ──────┬──── 🗡️ QuestRepository ────┬──── 📋 TaskRepository │
│     │                   │        │                   │        │            │
│     ├─ getHero()        │        ├─ createQuest()    │        ├─ getTasks() │
│     ├─ updateStats()    │        ├─ logEvent()       │        ├─ complete() │
│     ├─ saveProgress()   │        └─ getHistory()     │        └─ schedule()  │
│     └─ calculateLevel() │        │                   │        │            │
│     │                   │        │                   │        │            │
│     │                   │        ↓                   ↓        ↓            │
│  ⚡ StatusEffectRepository    📊 SettingsRepository                         │
│     │                            │                                          │
│     ├─ applyEffect()             ├─ getUserPrefs()                          │
│     ├─ checkExpiration()         ├─ saveConfig()                            │
│     └─ removeExpired()           └─ getTheme()                              │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                                        │
│                                                                             │
│  🗄️ AternaDatabase ───────┬──── 🌐 QuestApi ─────────┬──── ⚙️ Settings       │
│     │                    │        │                 │        │             │
│     ├─ HeroEntity        │        ├─ validateQuest() │        ├─ get()      │
│     ├─ QuestLogEntity    │        ├─ syncProgress()  │        ├─ set()      │
│     ├─ TaskEntity        │        └─ uploadResults() │        └─ clear()    │
│     ├─ StatusEffectEntity │       (Mock Implementation)       │             │
│     └─ Queries           │                                   │             │
│     │                    │                                   │             │
│     ↓                    ↓                                   ↓             │
│  📱 DatabaseDriverFactory  🔔 NotificationService                           │
│     │                        │                                             │
│     ├─ iOS Driver            ├─ iOS Notifier                               │
│     └─ Android Driver        └─ Android Notifier                           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        CRITICAL GAPS IDENTIFIED                            │
│                                                                             │
│  ❌ Missing: QuestStore implementation (referenced in DI but doesn't exist) │
│  ❌ Missing: OnboardingStore implementation (causes DI injection failure)   │
│  ❌ Missing: ViewModels layer (empty module in CommonKoinModule)            │
│  ❌ Missing: Quest execution UI (timer connects to planning, not execution) │
│  ❌ Missing: Quest Hub main screen (navigation exists, implementation doesn't)│
│  ⚠️  Fragile: Mock QuestApi used in production DI setup                    │
│  ⚠️  Fragile: Platform notifications exist but integration unclear          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          DEPENDENCY INJECTION FLOW                         │
│                                                                             │
│  🏗️ CommonKoinModule                                                        │
│     │                                                                       │
│     ├─ dataModule ─────────┬─── Provides: Repositories, Database, Settings │
│     │                      └─── Provides: TimeProvider, RewardService      │
│     │                                                                       │
│     ├─ domainModule ───────────── Provides: TaskNotificationService        │
│     │                                                                       │
│     ├─ notificationsModule ─────── Provides: Platform-specific notifiers   │
│     │                                                                       │
│     ├─ focusModule ────────────── Provides: QuestStore (❌ MISSING)        │
│     │                                                                       │
│     ├─ onboardingModule ───────── Provides: OnboardingStore (❌ MISSING)   │
│     │                                                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Key Dependency Relationships:**

1. **QuestStore** → HeroRepository + QuestRepository + RewardService + QuestPlanner
2. **QuestPlanner** → QuestResolver → RewardService → StatusEffectRepository
3. **UI Components** → Stores → Services → Repositories → Database/APIs
4. **Platform Layer** → Feature Layer → Domain Layer → Data Layer

**Critical Path for Implementation:**

1. Implement missing Store classes to fix DI failures
2. Create ViewModels to connect UI to domain logic
3. Build missing Quest Hub screen as central navigation point
4. Connect Timer selection to actual quest execution flow
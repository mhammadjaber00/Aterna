# Aterna Story System — Technical README

A deeply technical guide to implement the narrative layer cleanly on top of the existing quest/economy stack. Pairs with
**Aterna Story System README** (content pack + lines). This document covers:

- Module boundaries, public Kotlin APIs, and data contracts
- Determinism rules and PRNG usage
- SQLDelight schema (DDL), DAO queries, and repositories
- Event pipeline integration (with your existing `QuestEventsCoordinator`, `QuestStore`)
- Thread-building algorithms (pseudocode)
- Objective engine logic
- Content pack format (JSON/YAML) and validation
- Notifications & UI binding contracts
- Performance, concurrency, and testing strategy
- Migration & seeding steps (end-to-end)

> **Goal:** Keep economy & server parity *unchanged*. The story layer is additive, deterministic, and rebuildable from
> persisted data.

---

## 0) High-level data flow (ASCII)

```
Ticker ─┐
        ▼
QuestEventsCoordinator ──(QuestEvent*, ledger deltas)──▶ StoryOrchestrator
                                                       │
Hero/Quest flows ───────────────────────────────────────┘
         │
         ▼
StoryDirector (pure)
   ├─ reskin decisions for MOB
   ├─ ambient schedule/decisions
   └─ visit hooks (for objectives)
         │
         ▼
StoryEmitter (pure)
   ├─ creates StoryThread + StoryBeats (1–4)
   └─ adds System beats (banked/curse/progress)
         │
         ▼
StoryRepository (SQLDelight)
   ├─ story_threads
   ├─ story_beats
   ├─ story_event_defs / occurrences
   └─ objective_progress
         │
         ▼
UI (Compose)
   ├─ Adventure Log (threads)
   ├─ Portal ticker (latest beat)
   └─ Notification snippet (latest beat)
```

---

## 1) Modules & Public APIs

### 1.1 Module layout

```
:domain:story        // pure logic
:data:story          // repositories, SQLDelight DAOs
:features:story-ui   // Compose renderers for threads
:features:quest      // integration glue (StoryOrchestrator)
```

### 1.2 Kotlin interfaces (public surface)

```kotlin
package io.yavero.aterna.domain.story

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.quest.EventType
import kotlin.time.Instant

// ── Director (decisions only) ────────────────────────────────────────────────
interface StoryDirector {
    fun beginSession(hero: Hero, quest: Quest, now: Instant): SessionPlan
    fun decideForPlanEvent(
        session: SessionPlan,
        planIdx: Int,
        eventType: EventType,
        now: Instant
    ): ReskinDecision?
    fun maybeAmbient(session: SessionPlan, now: Instant): AmbientDecision?
}

data class SessionPlan(
    val questId: String,
    val biome: Biome,
    val ambientWindows: List<AmbientWindow>,
    val encounterRules: List<EncounterRule>,
    val seed: Long
)

enum class Biome { VILLAGE, FOREST }

data class AmbientWindow(val start: Instant, val end: Instant, val defIdCandidates: List<String>)

data class EncounterRule(
    val defId: String,                  // maps to story_event_defs.key
    val eligibleMobs: Set<String>,
    val majorOnly: Boolean,
    val minLevel: Int,
    val maxLevel: Int,
    val probabilityPerEvent: Double,
    val cooldownMinutes: Int
)

data class ReskinDecision(
    val encounterKey: String,           // story_event_defs.key
    val titleOverride: String? = null,
    val payload: Map<String, Any?> = emptyMap()
)

// Fired on tick within an ambient window.
data class AmbientDecision(val defKey: String)

// ── Emitter (creates UI rows) ────────────────────────────────────────────────
interface StoryEmitter {
    fun synthesizeStart(hero: Hero, quest: Quest, seed: Long, now: Instant): StoryThreadBuild
    fun fromPlanEvent(
        hero: Hero,
        quest: Quest,
        planIdx: Int,
        eventType: EventType,
        ledgerXp: Int,
        ledgerGold: Int,
        outcome: Outcome,
        reskin: ReskinDecision?,
        now: Instant
    ): StoryThreadBuild?
    fun fromAmbient(defKey: String, now: Instant): StoryThreadBuild
    fun synthesizeFinish(now: Instant): StoryThreadBuild
    fun synthesizeRetreat(context: RetreatContext, now: Instant): StoryThreadBuild
}

enum class Outcome { WIN, FLEE, NONE }

data class RetreatContext(val kind: RetreatKind, val bankedXp: Int, val bankedGold: Int, val curseMinutes: Int?)

enum class RetreatKind { QUICK, LATE, CURSED }

// Result to persist

data class StoryThreadBuild(
    val title: String,
    val source: StorySource,
    val refIdx: Int?,
    val beats: List<Beat>
)

enum class StorySource { PLAN_EVENT, STORY_EVENT, SYNTHETIC_START, SYN_END, SYN_RETREAT }

data class Beat(val kind: BeatKind, val text: String)

enum class BeatKind { Narration, Battle, Loot, Quirk, System }

// ── Objective Engine ────────────────────────────────────────────────────────
interface ObjectiveEngine {
    fun onMobOutcome(mob: String, outcome: Outcome, drops: Set<String>): List<ObjectiveDelta>
    fun onVisit(place: String): List<ObjectiveDelta>
    fun currentProgress(heroId: String): List<ObjectiveProgress>
}

data class ObjectiveDelta(val objectiveId: String, val delta: Int)

data class ObjectiveProgress(val objectiveId: String, val count: Int, val total: Int, val completed: Boolean)
```

### 1.3 Orchestrator (integration layer)

```kotlin
package io.yavero.aterna.features.story

import io.yavero.aterna.domain.story.*
import io.yavero.aterna.domain.model.quest.QuestEvent
import kotlinx.coroutines.flow.Flow

interface StoryOrchestrator {
    fun attach(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        questEventsFlow: Flow<List<QuestEvent>>, // per-quest preview or due events
        ticker: Flow<Instant>
    )
}
```

> The Orchestrator reacts to *resolved* quest events (as you already do in
`DefaultQuestEventsCoordinator.replayDueEventsWithLedger`) and produces story threads/beats.

---

## 2) Determinism & PRNG

- **Base seed** is already defined (see `QuestEconomyImpl.computeBaseSeed`). Reuse it.
- Construct per-scope seeds:

```kotlin
const val SEED_STRIDE = 1_337L

fun storySeed(baseSeed: Long) = baseSeed
fun perPlanEventSeed(baseSeed: Long, idx: Int) = baseSeed + idx * SEED_STRIDE + 17
fun ambientWindowSeed(baseSeed: Long, windowIndex: Int) = baseSeed + windowIndex * SEED_STRIDE + 23
```

- Use Kotlin `Random(seed)` or your `SplitMix64` wrapper consistently.
- All coin‑flips must be derived from these seeds to enable replay.

---

## 3) SQLDelight schema & DAO queries

The high-level DDL is in the functional README. Below are **DAO statements** you’ll likely want.

```sql
-- story_threads.sq (DAO)
selectThreadsByQuest
:
SELECT id, quest_id, source, ref_idx, title, order_idx, created_at
FROM story_threads
WHERE quest_id = ?
ORDER BY order_idx ASC;

insertThread
:
INSERT INTO story_threads(id, quest_id, source, ref_idx, title, order_idx, created_at)
VALUES (?, ?, ?, ?, ?, ?, ?);

-- story_beats.sq (DAO)
selectBeatsByThread
:
SELECT thread_id, idx, at, kind, text, tag
FROM story_beats
WHERE thread_id = ?
ORDER BY idx ASC;

insertBeat
:
INSERT INTO story_beats(thread_id, idx, at, kind, text, tag)
VALUES (?, ?, ?, ?, ?, ?);

-- occurrences
insertOccurrence
:
INSERT INTO story_event_occurrences(id, quest_id, def_id, at, outcome, delta_count, item_key)
VALUES (?, ?, ?, ?, ?, ?, ?);

-- objectives
selectObjectivesForChapter
:
SELECT id, type, target_key, target_count, display_text, reward_item_id, unlocks_chapter_id
FROM objectives
WHERE chapter_id = ?;

upsertObjectiveProgress
:
INSERT INTO objective_progress(hero_id, objective_id, count, completed_at)
VALUES (?, ?, ?, ?)
ON CONFLICT(hero_id, objective_id) DO
UPDATE SET
    count = excluded.count,
    completed_at = excluded.completed_at;
```

### 3.1 Repositories (interfaces)

```kotlin
interface StoryRepository {
    suspend fun insertThread(thread: StoryThreadEntity)
    suspend fun insertBeats(beats: List<StoryBeatEntity>)
    fun observeThreads(questId: String): Flow<List<StoryThreadWithBeats>>
    suspend fun nextOrderIndex(questId: String): Int
}

interface ChapterRepository {
    suspend fun getChapter(id: String): Chapter
    suspend fun getObjectives(chapterId: String): List<Objective>
}

interface ObjectiveRepository {
    suspend fun upsertProgress(heroId: String, objectiveId: String, delta: Int)
    fun observeProgress(heroId: String): Flow<List<ObjectiveProgress>>
}
```

Entity DTOs mirror the tables; keep them mechanical.

---

## 4) Event pipeline integration (with your code)

### 4.1 Where to hook

- You already rebuild deterministic events in `DefaultQuestEventsCoordinator.replayDueEventsWithLedger(...)`.
- After you append each `QuestEvent` to `QuestRepository.appendQuestEvent(ev)`, call **StoryOrchestrator**.

**Minimal diff (conceptual):**

```kotlin
val ev = QuestResolver.resolveFromLedger(...)
questRepository.appendQuestEvent(ev)

storyOrchestrator.onPlanEventResolved(
    hero = hero,
    quest = quest,
    planIdx = p.idx,
    eventType = p.type,
    ledgerXp = entry?.xpDelta ?: 0,
    ledgerGold = entry?.goldDelta ?: 0,
    outcome = when (ev.type) {
        EventType.MOB -> if (ev.outcome is EventOutcome.Flee) Outcome.FLEE else Outcome.WIN
        EventType.CHEST, EventType.QUIRKY, EventType.TRINKET -> Outcome.NONE
        else -> Outcome.NONE
    },
    now = ev.at
)
```

> Keep the ledger as the only truth for XP/GOLD. The story just mirrors it.

### 4.2 Start/Finish/Retreat hooks

- On quest start (inside `QuestActionServiceImpl.start` success): call `synthesizeStart`.
- On completion (`QuestActionServiceImpl.complete`), after notifications, call `synthesizeFinish`.
- On retreat, call `synthesizeRetreat` with `RetreatKind` chosen from your curse logic and pass banked totals.

---

## 5) StoryOrchestrator behavior (pseudocode)

```kotlin
class DefaultStoryOrchestrator(
    private val director: StoryDirector,
    private val emitter: StoryEmitter,
    private val storyRepo: StoryRepository,
    private val objEngine: ObjectiveEngine,
    private val time: TimeProvider
) : StoryOrchestrator {

    private var session: SessionPlan? = null
    private val orderIndices = mutableMapOf<String, Int>() // questId -> last order

    fun onStart(hero: Hero, quest: Quest) {
        val now = time.nowInstant()
        session = director.beginSession(hero, quest, now)
        val seed = QuestEconomyImpl.computeBaseSeed(hero, quest)
        persist(emitter.synthesizeStart(hero, quest, seed, now), quest.id, now)
    }

    fun onPlanEventResolved(
        hero: Hero, quest: Quest, planIdx: Int, eventType: EventType,
        ledgerXp: Int, ledgerGold: Int, outcome: Outcome, now: Instant
    ) {
        val s = session ?: return
        val reskin = director.decideForPlanEvent(s, planIdx, eventType, now)
        val build = emitter.fromPlanEvent(hero, quest, planIdx, eventType, ledgerXp, ledgerGold, outcome, reskin, now)
        if (build != null) persist(build, quest.id, now)

        // Objective bumps
        when (eventType) {
            EventType.MOB -> {
                val drops = emptySet<String>() // Optionally infer from reskin payload
                val deltas = objEngine.onMobOutcome(mobNameFor(ev), outcome, drops)
                applyObjectiveDeltas(quest.heroId, deltas, now)
            }
            EventType.TRINKET, EventType.QUIRKY -> {/* usually none */
            }
            EventType.CHEST -> {/* not chapter 1 */
            }
            else -> {}
        }
    }

    fun onAmbientTick(now: Instant) {
        val s = session ?: return
        director.maybeAmbient(s, now)?.let { dec ->
            persist(emitter.fromAmbient(dec.defKey, now), s.questId, now)
        }
    }

    fun onFinish(questId: String) {
        persist(emitter.synthesizeFinish(time.nowInstant()), questId, time.nowInstant())
    }
    fun onRetreat(questId: String, ctx: RetreatContext) {
        persist(emitter.synthesizeRetreat(ctx, time.nowInstant()), questId, time.nowInstant())
    }

    private suspend fun persist(build: StoryThreadBuild, questId: String, now: Instant) {
        val order = (orderIndices[questId] ?: storyRepo.nextOrderIndex(questId)) + 1
        orderIndices[questId] = order
        val threadId = Uuid.random().toString()
        storyRepo.insertThread(
            StoryThreadEntity(
                threadId,
                questId,
                build.source,
                build.refIdx,
                build.title,
                order,
                now
            )
        )
        storyRepo.insertBeats(build.beats.mapIndexed { idx, b ->
            StoryBeatEntity(
                threadId,
                idx,
                now,
                b.kind,
                b.text,
                null
            )
        })
    }

    private suspend fun applyObjectiveDeltas(
        heroId: String,
        deltas: List<ObjectiveDelta>,
        now: Instant
    ) { /* upsert & system beats via emitter if you wish */
    }
}
```

---

## 6) StoryEmitter mapping rules

- **START** → 1 beat from `openers.common` + class overlay (choose 1 deterministically).
- **MOB (win)** → 2–3 beats: Narration (optional), then named overlay win line (fallback generic). If reskinned, title
  uses encounter name.
- **MOB (flee)** → Flee line from named overlay (fallback generic). Ensure gold stays `0`.
- **CHEST** → 3–4 beats sequence (loot/empty) selected deterministically; last beat encodes `+$GOLD` if any.
- **QUIRKY** → 1 beat from `quirky` (carries `+$XP`).
- **TRINKET** → 1 beat from `trinket`.
- **VISIT(FOOTBRIDGE)** → 1–2 narration beats + System progress.
- **FINISH/RETREAT** → Use `closers` / `retreat.*` sets.
- **SYSTEM** (curse/banked/objectives) → choose from catalogs.

Selection uses `perPlanEventSeed(baseSeed, idx)` and `ambientWindowSeed` to index into the appropriate arrays.

---

## 7) ObjectiveEngine logic

- **KILL goblin x3**: increment by `+1` on MOB: GOBLIN + outcome=WIN; if reskin payload includes `GOBLIN_TAG` drop on
  win, you may also count that under COLLECT.
- **COLLECT wolf pelts x5**: increment by `+1` on MOB: WOLF + outcome=WIN + `drop_item_key="WOLF_PELT"` satisfied (drop
  chance comes from encounter defs).
- **VISIT footbridge x1**: fire via a **VISIT** synthetic trigger (e.g., when biome=FOREST and planIdx within last
  window, 1x per quest). Or tie to a QUIRKY/TRINKET with beats_key ‘FOOTBRIDGE_VISIT’.

**Data contracts:**

- `target_key` grammar: `mob:GOBLIN`, `item:WOLF_PELT`, `place:FOOTBRIDGE`.
- Objective deltas must be monotonic; never exceed `target_count`.

---

## 8) Content pack format (runtime)

- **Story event defs**

```json
{
  "key": "enc_goblin_lookout",
  // unique
  "kind": "ENCOUNTER",
  "eligible_mobs": [
    "GOBLIN"
  ],
  "major_only": true,
  "min_level": 1,
  "max_level": 15,
  "probability_per_event": 0.15,
  "cooldown_minutes": 20,
  "beats_key": "ENCOUNTER_GOBLIN_LOOKOUT",
  "payload": {
    "drop_item_key": "GOBLIN_TAG",
    "drop_chance": 0.35
  }
}
```

- **Ambient defs** similar with `kind:"AMBIENT"`, `beats_key:"VILLAGE_CAT"`.
- **Beats catalogs**: arrays of strings keyed by `beats_key`. Use placeholders; no numbers.

**Validation:** run style & duplication audit before seeding (see functional README §11).

---

## 9) Notifications & UI binding

- **Source of text**: latest beat of the most recently persisted thread for the active quest.
- **Ongoing notification**: pick from `ongoing_snippets` unless a fresh beat arrived in the last ~10s, in which case
  mirror that beat.
- **Adventure Log**: list of `StoryThreadWithBeats` ordered by `order_idx`, last expanded.
- **Objective chip**: render compact text from chip patterns + live progress from `objective_progress`.

---

## 10) Performance & concurrency

- **Writes**: only on start, plan event resolution, ambient fire, finish/retreat.
- **Order index**: compute once per persist via `storyRepo.nextOrderIndex(questId)`; cache in orchestrator to avoid N
  queries per thread.
- **Mutex**: share quest completion/retreat mutexes you already have; story writes are idempotent (use occurrence
  ids/hashes if needed).
- **Preview window**: keep your `PREVIEW_WINDOW = 1` for quest events; story doesn’t need it since threads are
  persisted.

---

## 11) Testing strategy

- **Determinism**: parameterized tests with fixed seeds; assert same threads/beats.
- **Parity**: sum of MOB win lines that include `+$XP, +$GOLD` equals ledger deltas for those planIdx.
- **Level-gate flee**: skeleton @ hero+3 → FLEE outcome; verify gold=0 and flee line used.
- **Process death**: kill mid-run; on load, `observeThreads(questId)` returns complete list; notification mirrors last
  beat.
- **Ambient rate**: simulate 60m quest; fired ambients per `probability_per_10m` within tolerance.
- **Objective progression**: run the JSON testcases from functional README §12.2.

---

## 12) Migration & seeding

1. **Migrate**: apply tables from functional README §4. No backfill required.
2. **Seed**: insert Chapter 1 (JSON) into `chapters`/`objectives`; insert ambient/encounter defs; ship catalogs as
   assets.
3. **Version**: store pack metadata (`pack_id = ch1_v1`) somewhere persistent to guard future updates.
4. **Roll-out**: behind a feature flag—story UI reads but doesn’t block the core quest loop if absent.

---

## 13) Example: building a MOB thread (win)

**Inputs**: planIdx=4, type=MOB(WOLF), ledgerXp=12, ledgerGold=5, outcome=WIN.

**Emitter steps**

1. Title: `"Wolf Pair"` if reskinned, else `"Wolf"`.
2. Narration (optional): pick a line from ambient to set scene (optional for 2+ beat threads).
3. Outcome line: select named overlay `WOLF.win` with `+$XP`, `+$GOLD`.
4. Persist as 2–3 beats.

**Persisted**

- `story_threads`: (id=T1, quest=Q, source=PLAN_EVENT, ref_idx=4, title="Wolf Pair", order=7)
- `story_beats`:
    - (T1, 0) kind=Battle text="You hold, they break. +$XP XP, +$GOLD gold."

> Replace placeholders with real numbers before persistence (use `ledgerXp`, `ledgerGold`).

---

## 14) Example: ambient firing

- On tick, `StoryDirector.maybeAmbient` checks if `now` is within any window and, with seeded RNG & cooldowns, chooses a
  `defKey`.
- Emitter uses `beats_key` (e.g., `VILLAGE_CAT`) to build a 1-beat Narration thread.
- Persist as `source=STORY_EVENT`.

---

## 15) Open questions & defaults

- **Where do VISIT events come from?** Option A: minor planIdx near end → synthesize VISIT:FOOTBRIDGE once per quest
  when Chapter 1 objective incomplete. Option B: ambient def with `kind:"CUTSCENE"` gating.
- **Drops mapping**: For Chapter 1, only WOLF→WOLF_PELT (chance up to 0.4) and GOBLIN→GOBLIN_TAG (0.25–0.35). Encode in
  `payload_json` of encounter defs.
- **Localization**: keep catalogs key-based; pass through i18n layer at emit time.

---

## 16) Done definition

- Threads & beats persist for start, each plan event (if applicable), ambient, finish/retreat.
- Objective progress persists and displays in chip + emits system beats.
- Latest beat mirrors in notification; never stalls.
- All outputs deterministic for a given quest seed; replayable after process death.

---

**Appendix A — Minimal data classes for entities**

```kotlin
data class StoryThreadEntity(
    val id: String,
    val questId: String,
    val source: StorySource,
    val refIdx: Int?,
    val title: String,
    val orderIdx: Int,
    val createdAt: Instant
)

data class StoryBeatEntity(
    val threadId: String,
    val idx: Int,
    val at: Instant,
    val kind: BeatKind,
    val text: String,
    val tag: String?
)

data class StoryThreadWithBeats(
    val thread: StoryThreadEntity,
    val beats: List<StoryBeatEntity>
)
```

**Appendix B — Content resolution helpers**

```kotlin
interface StoryCatalogs {
    fun opener(classType: ClassType, rng: Random): String
    fun closer(rng: Random): String
    fun retreat(kind: RetreatKind, rng: Random): String
    fun chest(empty: Boolean, rng: Random): List<String>
    fun mobOutcome(mob: String, outcome: Outcome, rng: Random): String
    fun quirky(rng: Random): String
    fun trinket(rng: Random): String
    fun system(type: SystemType, args: Map<String, Any?>, rng: Random): String
}

enum class SystemType { CURSE_APPLIED, LEVEL_UP, BANKED, OBJECTIVE_PROGRESS, OBJECTIVE_COMPLETE, CHAPTER_UNLOCKED }
```

**Appendix C — Placeholder substitution**

- Replace `$XP`, `$GOLD`, `$MOB_NAME`, `$OBJ_*`, `$CURSE_MINUTES` before persistence.
- Keep `$HERO_NAME` optional (some users pick a name later). Fallback to “You/Your”.

---

That’s the complete technical blueprint. Implementing the Orchestrator + Repos with this contract will keep your core
loop stable while unlocking story richness with zero economy risk.


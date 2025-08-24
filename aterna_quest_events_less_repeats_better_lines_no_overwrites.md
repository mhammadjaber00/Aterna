# Aterna Quest Events — Less Repeats, Better Lines, No Overwrites

This patch keeps your architecture (Planner → Economy/Allocator → Resolver → Coordinator) but fixes the three pain
points:

1) **Back‑to‑back repeats** → A deterministically randomized **anti‑repeat planner** with cooldowns.
2) **Boring / samey lines** → A **phrase engine** with large banks, slot‑filling, and synonyms; same event can render
   multiple different lines.
3) **Narration overwritten** → Narration events use **negative indexes** (−1, −2, …) so they never collide with planned
   beat indexes (0..N−1). Ordering is by `(at, idx)`.

You don’t need to change the ledger snapshot / reward allocator flow.

---

## 0) New tiny utilities

**`domain/util/TextRng.kt`** — deterministic picks based on `baseSeed` + `salt`

```kotlin
package io.yavero.aterna.domain.util

import kotlin.random.Random

class TextRng(private val seed: Long) {
    private fun r(salt: Long) = Random(seed xor salt)

    fun <T> pick(list: List<T>, salt: Long): T {
        if (list.isEmpty()) throw IllegalArgumentException("Empty list")
        val idx = r(salt).nextInt(list.size)
        return list[idx]
    }

    fun nextInt(bound: Int, salt: Long): Int = r(salt).nextInt(bound)

    fun chance(p: Double, salt: Long): Boolean {
        require(p in 0.0..1.0)
        return r(salt).nextDouble() < p
    }
}
```

**`domain/util/TextBits.kt`** — small helpers to vary wording

```kotlin
package io.yavero.aterna.domain.util

object TextBits {
    fun plural(n: Int, singular: String, plural: String = singular + "s") = if (n == 1) singular else plural
    fun commaJoin(parts: List<String>) = parts.filter { it.isNotBlank() }.joinToString(", ")
}
```

---

## 1) Phrase engine (lots of lines, slot‑filling)

**`domain/narrative/EventPhrases.kt`**

```kotlin
package io.yavero.aterna.domain.narrative

import io.yavero.aterna.domain.util.TextBits
import io.yavero.aterna.domain.util.TextRng

/**
 * Banks of templates & wordlists. Keep these growable.
 */
object EventPhrases {
    private val enemies = listOf(
        "gnarled task", "tiny dragon of distraction", "looming deadline", "ringing notification",
        "tab hydra", "scope creep", "maze of meetings", "bug gremlin", "spaghetti import"
    )
    private val verbsWin = listOf("crush", "slice", "outsmart", "outpace", "dodge", "parry", "tame")
    private val verbsSmall = listOf("nudge", "chip away at", "peek into", "tidy", "trim")
    private val trinkets = listOf("inked note", "lucky paperclip", "coffee bean", "polished thought", "sticky idea")
    private val places = listOf("old inbox", "foggy backlog", "focus lane", "quiet grove", "deep work cave")

    // Templates use {slots}. Keep counts modest, add more over time.
    private val mobTemplates = listOf(
        "You {verb} a {enemy}. +{xp} {xpWord}{goldMaybe}",
        "A {enemy} lunges—blocked! +{xp} {xpWord}{goldMaybe}",
        "Clean hit on the {enemy}. +{xp} {xpWord}{goldMaybe}",
        "You hold the line. {xp} {xpWord} secured{goldMaybe}"
    )
    private val chestTemplates = listOf(
        "You crack a side quest chest: +{gold} {goldWord}",
        "Hidden stash in the {place}: +{gold} {goldWord}",
        "Tucked under notes—coins! +{gold} {goldWord}"
    )
    private val quirkyTemplates = listOf(
        "Tiny win. +{xp} {xpWord}",
        "You {verbSmall} the mess. +{xp} {xpWord}",
        "A thought clicks into place. +{xp} {xpWord}"
    )
    private val trinketTemplates = listOf(
        "You pocket a {trinket}.",
        "Picked up a {trinket} near the path.",
        "A {trinket} rolls by—yours now."
    )

    fun mob(xp: Int, gold: Int, rng: TextRng, salt: Long): String {
        val t = rng.pick(mobTemplates, salt)
        val enemy = rng.pick(enemies, salt + 1)
        val verb = rng.pick(verbsWin, salt + 2)
        val xpWord = TextBits.plural(xp, "XP")
        val goldMaybe = if (gold > 0) ", +$gold ${TextBits.plural(gold, "gold")}" else ""
        return t
            .replace("{verb}", verb)
            .replace("{enemy}", enemy)
            .replace("{xp}", xp.toString())
            .replace("{xpWord}", xpWord)
            .replace("{goldMaybe}", goldMaybe)
    }

    fun chest(gold: Int, rng: TextRng, salt: Long): String {
        val t = rng.pick(chestTemplates, salt)
        val place = rng.pick(places, salt + 1)
        val goldWord = TextBits.plural(gold, "coin")
        return t
            .replace("{place}", place)
            .replace("{gold}", gold.toString())
            .replace("{goldWord}", goldWord)
    }

    fun quirky(xp: Int, rng: TextRng, salt: Long): String {
        val t = rng.pick(quirkyTemplates, salt)
        val verb = rng.pick(verbsSmall, salt + 1)
        return t
            .replace("{verbSmall}", verb)
            .replace("{xp}", xp.toString())
            .replace("{xpWord}", TextBits.plural(xp, "XP"))
    }

    fun trinket(rng: TextRng, salt: Long): String {
        val t = rng.pick(trinketTemplates, salt)
        val item = rng.pick(trinkets, salt + 1)
        return t.replace("{trinket}", item)
    }
}
```

---

## 2) Anti‑repeat deterministic planner (cooldowns)

*Guarantees*: no two adjacent events share the same `EventType`, plus per‑type cooldown window.

**`domain/service/quest/QuestPlanner.kt`** (drop‑in replacement of your `plan(...)` body)

```kotlin
package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.util.TextRng
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class QuestPlanner {
    data class Spec(
        val questId: String,
        val startAt: Instant,
        val duration: Duration,
        val classType: ClassType,
        val baseSeed: Long
    )

    @OptIn(ExperimentalTime::class)
    fun plan(spec: Spec): List<PlannedEvent> {
        val totalSec = max(30, spec.duration.inWholeSeconds.toInt())
        val beats = targetBeats(totalSec)
        if (beats <= 0) return emptyList()

        val step = (totalSec / (beats + 1)).coerceAtLeast(20)
        val rng = TextRng(spec.baseSeed)

        // Types pool with weights; tweak to taste
        val pool = listOf(
            Weighted(EventType.MOB, 3),
            Weighted(EventType.CHEST, 2),
            Weighted(EventType.QUIRKY, 2),
            Weighted(EventType.TRINKET, 1)
        )

        val cooldowns = mutableMapOf<EventType, Int>()
        val cdSize = 1 // how many beats before a type can repeat (adjacent=always blocked)

        val types = ArrayList<EventType>(beats)
        repeat(beats) { i ->
            // decrease cooldowns
            cooldowns.keys.toList().forEach { t -> cooldowns[t] = max(0, (cooldowns[t] ?: 0) - 1) }

            val last = types.lastOrNull()
            val options = pool.filter { w ->
                val cd = cooldowns[w.type] ?: 0
                val notSameAsLast = w.type != last
                val available = cd == 0
                notSameAsLast && available
            }

            val pick = if (options.isNotEmpty()) weightedPick(rng, options, salt = i.toLong())
            else weightedPick(rng, pool.filter { it.type != last }, salt = i.toLong()) // fallback only blocks adjacency

            types += pick.type
            cooldowns[pick.type] = cdSize
        }

        return types.mapIndexed { idx, t ->
            PlannedEvent(
                questId = spec.questId,
                idx = idx,
                dueAt = spec.startAt + (step * (idx + 1)).seconds,
                type = t,
                isMajor = (idx % 4 == 3 && t == EventType.MOB),
                mobTier = null
            )
        }
    }

    private fun targetBeats(totalSec: Int): Int = when (totalSec) {
        in 0..600 -> 6
        in 601..2100 -> 10
        in 2101..4500 -> 14
        else -> 16
    }

    private data class Weighted(val type: EventType, val weight: Int)

    private fun weightedPick(rng: TextRng, pool: List<Weighted>, salt: Long): Weighted {
        val sum = pool.sumOf { it.weight }
        var roll = rng.nextInt(sum, salt)
        for (w in pool) {
            if (roll < w.weight) return w
            roll -= w.weight
        }
        return pool.last()
    }
}
```

> Determinism: same `baseSeed` + same duration → identical plan; no adjacent duplicates; soft cooldowns reduce repeats.

---

## 3) Resolver that renders many different lines per event

**`domain/service/quest/QuestResolver.kt`** (only the message rendering changed; keep your deltas from the ledger)

```kotlin
package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.narrative.EventPhrases
import io.yavero.aterna.domain.util.TextRng
import kotlin.time.ExperimentalTime

class QuestResolver(private val baseSeed: Long) {
    private val rng = TextRng(baseSeed)

    /**
     * Create a rich message, deterministically varied per (idx, type).
     * xpDelta/goldDelta come from your RewardLedger.
     */
    @OptIn(ExperimentalTime::class)
    fun resolve(
        p: PlannedEvent,
        xpDelta: Int,
        goldDelta: Int
    ): QuestEvent {
        val msg = when (p.type) {
            EventType.MOB -> EventPhrases.mob(xpDelta, goldDelta, rng, salt = 10_000L + p.idx)
            EventType.CHEST -> EventPhrases.chest(goldDelta, rng, salt = 20_000L + p.idx)
            EventType.QUIRKY -> EventPhrases.quirky(xpDelta, rng, salt = 30_000L + p.idx)
            EventType.TRINKET -> EventPhrases.trinket(rng, salt = 40_000L + p.idx)
            EventType.NARRATION -> "" // not used here
        }

        return QuestEvent(
            questId = p.questId,
            idx = p.idx,
            at = p.dueAt,
            type = p.type,
            message = msg,
            xpDelta = xpDelta,
            goldDelta = goldDelta,
            outcome = null // keep your existing outcome logic if any
        )
    }
}
```

---

## 4) Narration never overwrites: use negative indexes

If your `quest_events` table has a unique key on `(questId, idx)`, a narration at `idx=0` will be **replaced** once the
planned beat #0 is written. Fix: assign narration `idx = -1, -2, ...` (distinct from 0..N−1).

**`features/quest/narrative/NarrativeAppender.kt`**

```kotlin
package io.yavero.aterna.features.quest.narrative

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.repository.QuestRepository
import kotlin.time.Instant

class NarrativeAppender(private val repo: QuestRepository) {
    suspend fun appendNarration(questId: String, text: String, at: Instant) {
        val nextNegIdx = -(1 + repo.countNarrationEvents(questId))
        val ev = QuestEvent(
            questId = questId,
            idx = nextNegIdx, // <—— negative index = never collides
            at = at,
            type = EventType.NARRATION,
            message = text,
            xpDelta = 0,
            goldDelta = 0,
            outcome = null
        )
        repo.appendQuestEvent(ev)
    }
}
```

**`domain/repository/QuestRepository.kt`** (new method)

```kotlin
interface QuestRepository {
    suspend fun appendQuestEvent(ev: QuestEvent)
    suspend fun countNarrationEvents(questId: String): Int // NEW
    fun getQuestEventsOrdered(questId: String): List<QuestEvent>
    fun getQuestEventsPreview(questId: String, limit: Int): List<QuestEvent>
}
```

**`data/repository/QuestRepositoryImpl.kt`** (relevant changes)

```kotlin
override suspend fun countNarrationEvents(questId: String): Int =
    queries.countNarration(questId).executeAsOne()

override fun getQuestEventsOrdered(questId: String): List<QuestEvent> =
    queries.selectEventsOrdered(questId).executeAsList().map(mapper)

override fun getQuestEventsPreview(questId: String, limit: Int): List<QuestEvent> =
    queries.selectEventsOrderedLimited(questId, limit.toLong()).executeAsList().map(mapper)
```

**SQLDelight** (add queries; adapt table/column names to yours)

```sql
-- Count narration with negative idx
countNarration
:
SELECT COUNT(*)
FROM quest_events
WHERE questId = ?
  AND type = 'NARRATION';

-- Order so narration (negative idx) stays where it happened in time
selectEventsOrdered
:
SELECT *
FROM quest_events
WHERE questId = ?
ORDER BY at ASC, idx ASC;

selectEventsOrderedLimited
:
SELECT *
FROM quest_events
WHERE questId = ?
ORDER BY at ASC, idx ASC
    LIMIT ?;
```

> If your insert used `INSERT OR REPLACE`, change it to **plain INSERT**. With unique `(questId, idx)` that’s now safe
> because narration uses negatives.

---

## 5) Coordinator: sort by `(at, idx)` for previews & notifications

**`domain/service/quest/DefaultQuestEventsCoordinator.kt`** (only the ordering line needs adjustment)

```kotlin
val preview = repo.getQuestEventsPreview(quest.id, 12)
    .sortedWith(compareBy({ it.at }, { it.idx })) // ensure narration appears correctly
val tail = preview.lastOrNull()?.message ?: "Adventuring..."
```

If you previously sorted only by `idx`, narration (negative) might bubble to the top—sorting by time first keeps the log
natural.

---

## 6) Wiring notes

- **Planner**: swap to the new `QuestPlanner.plan(Spec)` (same call site, just pass `baseSeed`).
- **Resolver**: your ledger still provides `xpDelta/goldDelta`; only message rendering changed.
- **Narration**: call `NarrativeAppender.appendNarration(...)` wherever you drop lines (start, loot, closer).
- **SQL**: ensure inserts are not REPLACE; add the `countNarration` and ordered selects.

---

## 7) Why this fixes your issues

- **Repeats**: The planner enforces adjacency block + small cooldown. With deterministic RNG, the plan is stable and
  varied.
- **Boring lines**: The phrase engine randomly picks templates + words per event using salts, so **the same event** gets
  **different lines** (but stays deterministic per (seed, idx)).
- **Narration overwritten**: Negative indexes ensure narration can’t collide with ledger‑replayed beats.
- **Design cleanups**: Variation logic is isolated (phrase engine), plan variety is centralized (planner), and ordering
  is explicit (time → idx).

---

## 8) Optional: add more spice quickly

Extend the banks in `EventPhrases` (enemies, verbs, places, trinkets) and add 3–5 more templates per type. Everything
else scales automatically.

```kotlin
// Example extra MOB template
"You outmaneuver the {enemy}. +{xp} {xpWord}{goldMaybe}"
```

That’s it. Drop these in and you should see: fewer repeats, richer logs, and narration staying put.


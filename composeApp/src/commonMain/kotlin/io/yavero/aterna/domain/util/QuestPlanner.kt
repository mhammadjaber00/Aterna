package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.MobTier
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.PlannerSpec
import io.yavero.aterna.domain.util.QuestPlanner.MINOR_BASE_S
import io.yavero.aterna.domain.util.QuestPlanner.MIN_GAP_S
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * # QuestPlanner
 *
 * Deterministically generates a timeline (plan) of quest events from a [PlannerSpec].
 *
 * ## Key properties
 * - **Deterministic**: All randomness is derived from [PlannerSpec.seed]. Given the same spec, the
 *   same event sequence is produced.
 * - **Seconds-based pacing**: Minor beats occur roughly every **15 ± 6 seconds** and never closer
 *   than **8 seconds** apart.
 * - **Major/minor mix**: A small, evenly distributed set of **major** beats is chosen across the
 *   timeline; all other beats are **minor**.
 * - **Event type distribution**:
 *   - Major: **80%** `MOB`, **20%** `CHEST`
 *   - Minor: **55%** `CHEST`, **25%** `QUIRKY`, **20%** `TRINKET`
 * - **Mob tiering**: Tier likelihood scales with planned duration; for mid-length quests we ensure
 *   at least one **MID** tier spawn.
 *
 * ## Target beat counts (by planned minutes)
 * - **0–10** → 4 beats (1 major)
 * - **11–35** → 7 beats (2 majors)
 * - **36–75** → 10 beats (3 majors, with at least one MID mob)
 * - **76+** → 16 beats (4 majors; adjust as needed)
 *
 * ## Threading
 * Pure, allocation-only logic. No I/O; safe to call on any thread.
 *
 * ## Example
 * ```
 * val spec = PlannerSpec(
 *   durationMinutes = 25,
 *   seed = 1234L,
 *   startAt = Clock.System.now(),
 *   heroLevel = 7,
 *   classType = ClassType.WARRIOR
 * )
 * val plan: List<PlannedEvent> = QuestPlanner.plan(spec)
 * ```
 */
@OptIn(ExperimentalTime::class)
object QuestPlanner {

    /** Base separation between minor beats, in seconds. */
    private const val MINOR_BASE_S = 15

    /** Uniform jitter applied around [MINOR_BASE_S], in seconds. */
    private const val MINOR_JITTER_S = 6

    /** Minimum enforced gap between consecutive beats, in seconds. */
    private const val MIN_GAP_S = 8

    /**
     * Builds a deterministic sequence of [PlannedEvent] from the provided [PlannerSpec].
     *
     * The algorithm:
     * 1. Chooses a target number of beats based on `durationMinutes`.
     * 2. Generates a seconds-based beat timeline using 15 ± 6s deltas, enforcing a ≥8s minimum gap.
     * 3. Evenly spreads a small set of "major" indices across the timeline.
     * 4. Assigns each beat an [EventType] according to the major/minor distributions.
     * 5. For `MOB` events, picks a [MobTier] tuned to `durationMinutes` and ensures at least one
     *    `MID` tier in the 36–75 minute bucket.
     *
     * All randomness is derived from [PlannerSpec.seed].
     *
     * @param spec The planning inputs (duration, start time, seed, etc.).
     * @return A list of [PlannedEvent] ordered by ascending `idx`. Each event’s `questId` is left
     *         empty and should be filled by the caller before persistence.
     */
    fun plan(spec: PlannerSpec): List<PlannedEvent> {
        val rng = Random(spec.seed)

        val totalBeats = targetEventCount(spec.durationMinutes)
        val majorBeats = targetMajorCount(spec.durationMinutes)

        val beatTimes = buildBeatTimeline(
            startAt = spec.startAt,
            total = totalBeats,
            rng = rng
        )

        val majorSlots = evenlySpacedIndices(totalBeats, majorBeats)

        var placedMidOnce = false

        return beatTimes.mapIndexed { index, dueAt ->
            val isMajor = index in majorSlots
            val type = pickEventType(isMajor, rng)

            val tier = if (type == EventType.MOB) {
                pickMobTier(spec.durationMinutes, rng, placedMidOnce).also {
                    if (it == MobTier.MID) placedMidOnce = true
                }
            } else null

            PlannedEvent(
                questId = "",
                idx = index,
                dueAt = dueAt,
                type = type,
                isMajor = isMajor,
                mobTier = tier
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Beat counts & distribution
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns the total number of beats to schedule for a given planned duration.
     *
     * @param durationMinutes Planned quest length, in minutes.
     * @return Target beat count (minor + major).
     */
    private fun targetEventCount(durationMinutes: Int): Int = when (durationMinutes) {
        in 0..10 -> 4
        in 11..35 -> 7
        in 36..75 -> 10
        else -> 16
    }

    /**
     * Returns the number of **major** beats to place for the given planned duration.
     *
     * @param durationMinutes Planned quest length, in minutes.
     * @return Major beat count.
     */
    private fun targetMajorCount(durationMinutes: Int): Int = when (durationMinutes) {
        in 0..10 -> 1
        in 11..35 -> 2
        in 36..75 -> 3
        else -> 4
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Timeline construction
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Produces a list of beat times starting after [startAt], using 15 ± 6s spacing with a
     * ≥8s minimum enforced between consecutive beats.
     *
     * @param startAt Quest start timestamp.
     * @param total Total beats to produce.
     * @param rng Random source seeded from [PlannerSpec.seed].
     * @return A strictly non-decreasing list of [Instant]s of size [total].
     */
    private fun buildBeatTimeline(startAt: Instant, total: Int, rng: Random): List<Instant> {
        val times = ArrayList<Instant>(total)
        var t = startAt + jitteredDuration(MINOR_BASE_S, MINOR_JITTER_S, rng)
        repeat(total) {
            times += t
            val delta = jitteredDuration(MINOR_BASE_S, MINOR_JITTER_S, rng)
            t = (t + delta).ensureMinGapSince(times.last(), MIN_GAP_S.seconds)
        }
        return times
    }

    /**
     * Picks an [EventType] given whether a beat is major or minor.
     *
     * @param isMajor Whether the current beat is marked as major.
     * @param rng Random source.
     * @return The selected [EventType].
     */
    private fun pickEventType(isMajor: Boolean, rng: Random): EventType {
        if (isMajor) {
            // Major: 80% Mob, 20% Chest
            return if (rng.nextDouble() < 0.8) EventType.MOB else EventType.CHEST
        }
        // Minor: 55% Chest, 25% Quirky, 20% Trinket
        val p = rng.nextDouble()
        return when {
            p < 0.55 -> EventType.CHEST
            p < 0.80 -> EventType.QUIRKY
            else -> EventType.TRINKET
        }
    }

    /**
     * Chooses a [MobTier] based on planned duration. For 36–75 minute quests,
     * guarantees at least one **MID** tier across the timeline by returning `MID`
     * the first time this is encountered.
     *
     * @param durationMinutes Planned quest length, in minutes.
     * @param rng Random source.
     * @param placedMidOnce Whether a `MID` tier has already been placed earlier in the plan.
     * @return The selected [MobTier].
     */
    private fun pickMobTier(
        durationMinutes: Int,
        rng: Random,
        placedMidOnce: Boolean
    ): MobTier = when (durationMinutes) {
        in 0..10 -> MobTier.LIGHT
        in 11..35 -> if (rng.nextDouble() < 0.20) MobTier.MID else MobTier.LIGHT
        in 36..75 -> when {
            !placedMidOnce -> MobTier.MID
            rng.nextDouble() < 0.15 -> MobTier.MID
            else -> MobTier.LIGHT
        }

        else -> {
            val r = rng.nextDouble()
            when {
                r < 0.10 -> MobTier.RARE
                r < 0.50 -> MobTier.MID
                else -> MobTier.LIGHT
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns a non-negative [Duration] centered around [baseSeconds] with uniform jitter of
     * ±[jitterSeconds]. The result is clamped to be at least [MIN_GAP_S] seconds.
     *
     * @param baseSeconds Base seconds around which to jitter.
     * @param jitterSeconds Maximum absolute jitter in seconds.
     * @param rng Random source.
     */
    private fun jitteredDuration(baseSeconds: Int, jitterSeconds: Int, rng: Random): Duration {
        val offset = rng.nextInt(-jitterSeconds, jitterSeconds + 1)
        return (baseSeconds + offset).coerceAtLeast(MIN_GAP_S).seconds
    }

    /**
     * Ensures this [Instant] occurs at least [minGap] after [prev]. If it does not,
     * returns `prev + minGap`.
     *
     * @param prev Previous beat time.
     * @param minGap Minimum allowed gap.
     */
    private fun Instant.ensureMinGapSince(prev: Instant, minGap: Duration): Instant =
        if (this - prev < minGap) prev + minGap else this

    /**
     * Computes a set of [wanted] indices evenly spaced across the range `[0, total)`.
     * Useful for distributing major beats. If [wanted] ≥ [total], returns all indices.
     *
     * @param total Total slots available.
     * @param wanted Number of indices to select.
     * @return A set of selected indices in ascending order.
     */
    private fun evenlySpacedIndices(total: Int, wanted: Int): Set<Int> {
        if (wanted <= 0 || total <= 0) return emptySet()
        if (wanted >= total) return (0 until total).toSet()
        val step = total.toDouble() / (wanted + 1)
        return (1..wanted).map { k -> (k * step).toInt().coerceIn(0, total - 1) }.toSet()
    }
}
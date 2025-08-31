@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.quest.narrative

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.util.TextBits
import io.yavero.aterna.domain.util.TextRng
import kotlin.random.Random

/**
 * Unified text generator for BOTH:
 *  - freeform narration (start, closer, loot gain)
 *  - event messages (mob/chest/quirky/trinket)
 *
 * Deterministic where it matters (seed + idx) so previews match commits.
 */
object Narrator {

    // ===== Public entry points =====

    fun startLine(classType: ClassType, heroName: String, salt: Long? = null): String? {
        val cat = when (classType) {
            ClassType.WARRIOR -> Category.StartWarrior
            ClassType.MAGE -> Category.StartMage
        }
        return pickWeighted(cat, mapOf("HERO_NAME" to heroName), salt)
            ?: pickWeighted(Category.StartCommon, mapOf("HERO_NAME" to heroName), salt)
    }

    fun closerLine(salt: Long? = null): String? = pickWeighted(Category.Closer, emptyMap(), salt)

    fun lootGainLine(itemNamesCsv: String, salt: Long? = null): String? =
        pickWeighted(Category.LootGain, mapOf("ITEMS" to itemNamesCsv), salt)

    /**
     * Builds the line for a planned quest event using deterministic salt.
     * @param rngSeed  base quest seed (e.g., baseSeed)
     * @param idx      event index
     * @param flee     mob fled?
     * @param enemyOverride if non-null, use this mob name instead of the generic "enemy" list
     */
    fun eventLine(
        type: EventType,
        xp: Int,
        gold: Int,
        rngSeed: Long,
        idx: Int,
        flee: Boolean = false,
        enemyOverride: String? = null
    ): String = when (type) {
        EventType.MOB -> mobMessage(xp, gold, rngSeed, idx, flee, enemyOverride)
        EventType.CHEST -> chestMessage(gold, rngSeed, idx)
        EventType.QUIRKY -> quirkyMessage(xp, rngSeed, idx)
        EventType.TRINKET -> trinketMessage(rngSeed, idx)
        EventType.NARRATION -> "" // DB-appended narration records set message directly
    }

    // ===== Event messages =====

    private fun mobMessage(
        xp: Int,
        gold: Int,
        rngSeed: Long,
        idx: Int,
        flee: Boolean,
        enemyOverride: String?
    ): String {
        if (flee) return "Above your pay grade. You retreat with dignity. +$xp XP."
        val rng = TextRng(rngSeed xor (10_000L + idx))
        val t = pick(mobTemplates, rng, 0)
        val enemy = enemyOverride ?: pick(enemies, rng, 1)
        val verb = pick(verbsWin, rng, 2)
        val xpWord = TextBits.plural(xp, "XP")
        val goldMaybe = if (gold > 0) ", +$gold ${TextBits.plural(gold, "gold")}" else ""
        return t.replace("{verb}", verb)
            .replace("{enemy}", enemy)
            .replace("{xp}", xp.toString())
            .replace("{xpWord}", xpWord)
            .replace("{goldMaybe}", goldMaybe)
    }

    private fun chestMessage(gold: Int, rngSeed: Long, idx: Int): String {
        val rng = TextRng(rngSeed xor (20_000L + idx))
        val t = pick(chestTemplates, rng, 0)
        val place = pick(places, rng, 1)
        val goldWord = TextBits.plural(gold, "coin")
        return t.replace("{place}", place)
            .replace("{gold}", gold.toString())
            .replace("{goldWord}", goldWord)
    }

    private fun quirkyMessage(xp: Int, rngSeed: Long, idx: Int): String {
        val rng = TextRng(rngSeed xor (30_000L + idx))
        val t = pick(quirkyTemplates, rng, 0)
        val verb = pick(verbsSmall, rng, 1)
        return t.replace("{verbSmall}", verb)
            .replace("{xp}", xp.toString())
            .replace("{xpWord}", TextBits.plural(xp, "XP"))
    }

    private fun trinketMessage(rngSeed: Long, idx: Int): String {
        val rng = TextRng(rngSeed xor (40_000L + idx))
        val t = pick(trinketTemplates, rng, 0)
        val item = pick(trinkets, rng, 1)
        return t.replace("{trinket}", item)
    }

    // ===== Internals: freeform narration picker =====

    enum class Category { StartCommon, StartWarrior, StartMage, LootGain, Closer }

    data class Line(val text: String, val weight: Int = 100)

    private val recent = mutableMapOf<Category, ArrayDeque<String>>()

    private fun remember(cat: Category, picked: String, keep: Int = 8) {
        val q = recent.getOrPut(cat) { ArrayDeque() }
        q.remove(picked); q.addFirst(picked)
        while (q.size > keep) q.removeLast()
    }

    private fun pickWeighted(cat: Category, tokens: Map<String, String>, salt: Long?): String? {
        val pool = pools[cat] ?: return null
        val avoid = recent[cat]?.toSet() ?: emptySet()
        val shortlist = pool.filter { it.text !in avoid }.ifEmpty { pool }
        val chosen = weightedPick(shortlist, salt) ?: return null
        remember(cat, chosen.text)
        return tokens.entries.fold(chosen.text) { acc, (k, v) -> acc.replace("\$$k", v) }
    }

    private fun weightedPick(lines: List<Line>, salt: Long?): Line? {
        if (lines.isEmpty()) return null
        val r = if (salt != null) Random(salt) else Random
        val total = lines.sumOf { it.weight }
        var x = r.nextInt(total)
        for (l in lines) {
            x -= l.weight; if (x < 0) return l
        }
        return lines.last()
    }

    private fun <T> pick(list: List<T>, rng: TextRng, salt: Long): T = rng.pick(list, salt)

    // ===== Copy of phrase pools (from Narrative + EventPhrases) =====

    private val enemies = listOf(
        "gnarled task", "tiny dragon of distraction", "looming deadline", "ringing notification",
        "tab hydra", "scope creep", "maze of meetings", "bug gremlin", "spaghetti import"
    )
    private val verbsWin = listOf("crush", "slice", "outsmart", "outpace", "dodge", "parry", "tame")
    private val verbsSmall = listOf("nudge", "chip away at", "peek into", "tidy", "trim")
    private val trinkets = listOf("inked note", "lucky paperclip", "coffee bean", "polished thought", "sticky idea")
    private val places = listOf("old inbox", "foggy backlog", "focus lane", "quiet grove", "deep work cave")

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

    private val pools: Map<Category, List<Line>> = mapOf(
        Category.StartCommon to listOf(
            Line("\$HERO_NAME checks the sky. New quest."), Line("Boots on. World off. Quest on."),
            Line("You breathe in. It tastes like trouble."), Line("A list in your head. A path underfoot."),
            Line("You tie a knot, tighter than needed."), Line("You speak a promise no one hears."),
            Line("You stretch the ache out of your hands."), Line("Maps? Later. Feet? Now."),
            Line("You touch the charm in your pocket."), Line("Quiet morning. Loud plans."),
            Line("You nod to nobody in particular."), Line("A sparrow heckles you. You go anyway."),
            Line("The gate creaks a blessing as you pass."), Line("You count steps until you stop counting."),
            Line("Fresh mud. Old habits."), Line("You choose forward. Again."),
            Line("You breathe out and begin."), Line("A thread of luck catches on you."),
            Line("You pocket a pencil and a hope."), Line("Sunlight snags on your sleeve."),
            Line("The road pretends it didn’t miss you."), Line("You shrug off the last excuse."),
            Line("You clock the wind. It’s with you."), Line("Another run. Keep it simple."),
            Line("Somewhere, future-you smiles. Go meet them.", 30),
            Line("Today owes you nothing. You go anyway.", 30),
            Line("The world blinks first.", 8)
        ),
        Category.StartWarrior to listOf(
            Line("\$HERO_NAME cracks knuckles. Time to work."), Line("Straps tight. Edges keen. Go."),
            Line("You shoulder the day like a shield."), Line("Steel’s quiet. That’s the good sign."),
            Line("You roll your neck. The world flinches."), Line("Old dents, new reasons."),
            Line("You trust your boots more than luck."), Line("Grip. Stance. Forward."),
            Line("You nod to the training yard and pass it."), Line("A breath. A step. A promise kept."),
            Line("You keep the blade sheathed. For now."), Line("Shoulder pops. Road answers."),
            Line("You walk like a door through fog."), Line("Rust scrapes off your courage."),
            Line("You count scars like prayers."), Line("Leather creaks. Day starts."),
            Line("You carry quiet like a weapon."), Line("You pick a fight with the distance."),
            Line("Knots and buckles. Then earth."), Line("You grip the hilt. Let go. Move."),
            Line("You stare the morning down first."), Line("You tuck strength into your stride."),
            Line("You go where trouble went."), Line("If it bleeds, it budgets time for you.", 30),
            Line("Sword stays hungry. You feed it tasks.", 8)
        ),
        Category.StartMage to listOf(
            Line("\$HERO_NAME snaps a page shut. Field work."), Line("Ink dries. Curiosity doesn’t."),
            Line("You pocket chalk and a theory."), Line("You hum a pattern, then hush."),
            Line("You taste the air for answers."), Line("You fold a note into your sleeve."),
            Line("You count aloud. The day pretends not to listen."), Line("You trace a sigil only you can see."),
            Line("Your satchel argues. You win."), Line("You seal a jar of patience."),
            Line("You mark today: experiment."), Line("You press a pebble for luck."),
            Line("You list variables. You go anyway."), Line("You bait the unknown with a grin."),
            Line("Theory is warm. Roads are warmer."), Line("You set a ward on your nerves."),
            Line("You uncurl a question like a map."), Line("You shake crumbs off your notes."),
            Line("You whisper, then pretend you didn’t."), Line("You choose messy discovery."),
            Line("You keep one eye on the horizon."), Line("You pack extra chalk. Just in case."),
            Line("You measure nothing. You start."), Line("You tuck wonder behind your ear."),
            Line("You bribe the void with curiosity.", 30),
            Line("Equations gossip as you pass.", 8)
        ),
        Category.LootGain to listOf(
            Line("You rummage and grin: \$ITEMS."), Line("Pack’s heavier. Worth it: \$ITEMS."),
            Line("Shiny problem solved: \$ITEMS."), Line("You tag today with \$ITEMS."),
            Line("Treasure says hello: \$ITEMS.", 30), Line("The road tips you: \$ITEMS.", 8)
        ),
        Category.Closer to listOf(
            Line("Back to camp. Boots dusty."), Line("You call it. Good run."),
            Line("Firelight wins. You head home."), Line("You pocket the day and zip it."),
            Line("Enough for now. You’ll be back."), Line("You leave the path where it lies."),
            Line("You bow to the evening."), Line("You trade road for rest."),
            Line("Notes filed. Feet done."), Line("You hand the day a nod."),
            Line("You earned the quiet."), Line("You save a little luck for later."),
            Line("You hang up your thoughts."), Line("You bank the small win."),
            Line("Camp smells like relief."), Line("You pass the gate and grin."),
            Line("You set the pack down first."), Line("You call dibs on sleep."),
            Line("Good work. Lights down."), Line("You shelve the road for tomorrow."),
            Line("You thank your knees."), Line("You hush the hunger with plans."),
            Line("You melt into a chair."), Line("Gonna catch some zzz."),
            Line("The map purrs when folded right.", 30),
            Line("Moon signs your timesheet.", 8)
        )
    )
}

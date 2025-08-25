package io.yavero.aterna.domain.narrative

import io.yavero.aterna.domain.model.ClassType
import kotlin.random.Random

object Narrative {

    enum class Category { StartCommon, StartWarrior, StartMage, LootGain, Closer, Mob, Chest, Quirky }

    data class Line(val text: String, val weight: Int = 100)

    private val recent = mutableMapOf<Category, ArrayDeque<String>>()

    private fun remember(cat: Category, picked: String, keep: Int = 8) {
        val q = recent.getOrPut(cat) { ArrayDeque() }
        q.remove(picked); q.addFirst(picked)
        while (q.size > keep) q.removeLast()
    }

    private fun replaceTokens(s: String, tokens: Map<String, String>) =
        tokens.entries.fold(s) { acc, (k, v) -> acc.replace("$$k", v) }

    fun pickWeighted(cat: Category, tokens: Map<String, String> = emptyMap()): String? {
        val pool = pools[cat] ?: return null
        val avoid = recent[cat]?.toSet() ?: emptySet()
        val shortlist = pool.filter { it.text !in avoid }.ifEmpty { pool }
        val chosen = weightedPick(shortlist) ?: return null
        remember(cat, chosen.text)
        return replaceTokens(chosen.text, tokens)
    }

    private fun weightedPick(lines: List<Line>): Line? {
        if (lines.isEmpty()) return null
        val total = lines.sumOf { it.weight }
        var r = Random.nextInt(total)
        for (l in lines) {
            r -= l.weight; if (r < 0) return l
        }
        return lines.last()
    }

    fun startCategoryFor(classType: ClassType): Category =
        when (classType) {
            ClassType.WARRIOR -> Category.StartWarrior
            ClassType.MAGE -> Category.StartMage
        }


    private val pools: Map<Category, List<Line>> = mapOf(
        Category.StartCommon to listOf(
            Line("\$HERO_NAME checks the sky. New quest.", 100),
            Line("Boots on. World off. Quest on.", 100),
            Line("You breathe in. It tastes like trouble.", 100),
            Line("A list in your head. A path underfoot.", 100),
            Line("You tie a knot, tighter than needed.", 100),
            Line("You speak a promise no one hears.", 100),
            Line("You stretch the ache out of your hands.", 100),
            Line("Maps? Later. Feet? Now.", 100),
            Line("You touch the charm in your pocket.", 100),
            Line("Quiet morning. Loud plans.", 100),
            Line("You nod to nobody in particular.", 100),
            Line("A sparrow heckles you. You go anyway.", 100),
            Line("The gate creaks a blessing as you pass.", 100),
            Line("You count steps until you stop counting.", 100),
            Line("Fresh mud. Old habits.", 100),
            Line("You choose forward. Again.", 100),
            Line("You breathe out and begin.", 100),
            Line("A thread of luck catches on you.", 100),
            Line("You pocket a pencil and a hope.", 100),
            Line("Sunlight snags on your sleeve.", 100),
            Line("The road pretends it didn’t miss you.", 100),
            Line("You shrug off the last excuse.", 100),
            Line("You clock the wind. It’s with you.", 100),
            Line("Another run. Keep it simple.", 100),
            Line("Somewhere, future‑you smiles. Go meet them.", 30),
            Line("Today owes you nothing. You go anyway.", 30),
            Line("The world blinks first.", 8)
        ),
        Category.StartWarrior to listOf(
            Line("\$HERO_NAME cracks knuckles. Time to work.", 100),
            Line("Straps tight. Edges keen. Go.", 100),
            Line("You shoulder the day like a shield.", 100),
            Line("Steel’s quiet. That’s the good sign.", 100),
            Line("You roll your neck. The world flinches.", 100),
            Line("Old dents, new reasons.", 100),
            Line("You trust your boots more than luck.", 100),
            Line("Grip. Stance. Forward.", 100),
            Line("You nod to the training yard and pass it.", 100),
            Line("A breath. A step. A promise kept.", 100),
            Line("You keep the blade sheathed. For now.", 100),
            Line("Shoulder pops. Road answers.", 100),
            Line("You walk like a door through fog.", 100),
            Line("Rust scrapes off your courage.", 100),
            Line("You count scars like prayers.", 100),
            Line("Leather creaks. Day starts.", 100),
            Line("You carry quiet like a weapon.", 100),
            Line("You pick a fight with the distance.", 100),
            Line("Knots and buckles. Then earth.", 100),
            Line("You grip the hilt. Let go. Move.", 100),
            Line("You stare the morning down first.", 100),
            Line("You tuck strength into your stride.", 100),
            Line("You go where trouble went.", 100),
            Line("If it bleeds, it budgets time for you.", 30),
            Line("Sword stays hungry. You feed it tasks.", 8)
        ),
        Category.StartMage to listOf(
            Line("\$HERO_NAME snaps a page shut. Field work.", 100),
            Line("Ink dries. Curiosity doesn’t.", 100),
            Line("You pocket chalk and a theory.", 100),
            Line("You hum a pattern, then hush.", 100),
            Line("You taste the air for answers.", 100),
            Line("You fold a note into your sleeve.", 100),
            Line("You count aloud. The day pretends not to listen.", 100),
            Line("You trace a sigil only you can see.", 100),
            Line("Your satchel argues. You win.", 100),
            Line("You seal a jar of patience.", 100),
            Line("You mark today: experiment.", 100),
            Line("You press a pebble for luck.", 100),
            Line("You list variables. You go anyway.", 100),
            Line("You bait the unknown with a grin.", 100),
            Line("Theory is warm. Roads are warmer.", 100),
            Line("You set a ward on your nerves.", 100),
            Line("You uncurl a question like a map.", 100),
            Line("You shake crumbs off your notes.", 100),
            Line("You whisper, then pretend you didn’t.", 100),
            Line("You choose messy discovery.", 100),
            Line("You keep one eye on the horizon.", 100),
            Line("You pack extra chalk. Just in case.", 100),
            Line("You measure nothing. You start.", 100),
            Line("You tuck wonder behind your ear.", 100),
            Line("You bribe the void with curiosity.", 30),
            Line("Equations gossip as you pass.", 8)
        ),
        Category.LootGain to listOf(
            Line("You rummage and grin: \$ITEMS.", 100),
            Line("Pack’s heavier. Worth it: \$ITEMS.", 100),
            Line("Shiny problem solved: \$ITEMS.", 100),
            Line("You tag today with \$ITEMS.", 100),
            Line("Treasure says hello: \$ITEMS.", 30),
            Line("The road tips you: \$ITEMS.", 8)
        ),
        Category.Mob to listOf(
            Line("You trade effort for progress.", 100),
            Line("You keep swinging; it keeps shrinking.", 100),
            Line("Boredom ambush repelled.", 100),
            Line("You out‑stubborn the obstacle.", 30)
        ),
        Category.Chest to listOf(
            Line("Loose brick; lucky find.", 100),
            Line("Small stash, big mood.", 100),
            Line("Pocket change, pocket smile.", 30)
        ),
        Category.Quirky to listOf(
            Line("A crow judges your posture. You ignore it.", 100),
            Line("You misplace a thought and find momentum.", 100),
            Line("A cloud looks like a checkbox. You tick it.", 30),
            Line("You step on a legend and keep it quiet.", 8)
        ),
        Category.Closer to listOf(
            Line("Back to camp. Boots dusty.", 100),
            Line("You call it. Good run.", 100),
            Line("Firelight wins. You head home.", 100),
            Line("You pocket the day and zip it.", 100),
            Line("Enough for now. You’ll be back.", 100),
            Line("You leave the path where it lies.", 100),
            Line("You bow to the evening.", 100),
            Line("You trade road for rest.", 100),
            Line("Notes filed. Feet done.", 100),
            Line("You hand the day a nod.", 100),
            Line("You earned the quiet.", 100),
            Line("You save a little luck for later.", 100),
            Line("You hang up your thoughts.", 100),
            Line("You bank the small win.", 100),
            Line("Camp smells like relief.", 100),
            Line("You pass the gate and grin.", 100),
            Line("You set the pack down first.", 100),
            Line("You call dibs on sleep.", 100),
            Line("Good work. Lights down.", 100),
            Line("You shelve the road for tomorrow.", 100),
            Line("You thank your knees.", 100),
            Line("You hush the hunger with plans.", 100),
            Line("You melt into a chair.", 100),
            Line("Gonna catch some zzz.", 100),
            Line("The map purrs when folded right.", 30),
            Line("Moon signs your timesheet.", 8)
        )
    )
}

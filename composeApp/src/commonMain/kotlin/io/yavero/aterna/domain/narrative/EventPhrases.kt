package io.yavero.aterna.domain.narrative

import io.yavero.aterna.domain.util.TextBits
import io.yavero.aterna.domain.util.TextRng


object EventPhrases {
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
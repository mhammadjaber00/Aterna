package io.yavero.aterna.features.hero_stats

/** The seven RPG attributes (SPECIAL-style). */
enum class SpecialAttr(val short: String, val label: String) {
    STR("STR", "Strength"),
    PER("PER", "Perception"),
    END("END", "Endurance"),
    CHA("CHA", "Charisma"),
    INT("INT", "Intelligence"),
    AGI("AGI", "Agility"),
    LUCK("LUCK", "Luck")
}

/** What the Hero screen needs to render one attribute chip/ring. */
data class AttrUi(
    val kind: SpecialAttr,
    val rank: Int,
    val label: String = kind.label,
    val short: String = kind.short,
    /** Progress toward next rank (0f..1f). If you don’t track residues yet, just pass 0f. */
    val progressToNext: Float = 0f
)
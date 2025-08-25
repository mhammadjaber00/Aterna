package io.yavero.aterna.domain.util

object QuestStrings {

    object MobNames {
        const val GOBLIN = "Goblin"
        const val WOLF = "Wolf"
        const val SKELETON = "Skeleton"
        const val OGRE = "Ogre"
        const val WRAITH = "Wraith"
        const val DRAGON = "Dragon"
        const val ANCIENT_GOLEM = "Ancient Golem"

        val LIGHT_TIER = listOf(GOBLIN, WOLF, SKELETON)
        val MID_TIER = listOf(OGRE, WRAITH)
        val RARE_TIER = listOf(DRAGON, ANCIENT_GOLEM)
    }


    object CombatMessages {
        const val RETREAT_TEMPLATE = "Above your pay grade. You retreat with dignity. +%d XP."
        const val DEFEAT_TEMPLATE = "%s defeated. +%d XP, +%d gold."
    }


    object ChestMessages {
        const val RICH_CHEST = "Rich chest"
        const val LOOSE_BRICK = "Loose brick"
        const val CHEST_GOLD_TEMPLATE = "%s hides %d gold."
    }


    object QuirkyMessages {
        const val AGGRO_MUSHROOM_TEMPLATE = "An Aggro Mushroom postures. You bop it. +%d XP."
        const val MIMIC_TEMPLATE = "A squeaky mimic tries to be a chest. Bad job. +%d XP."
        const val STONES_WHISPER_TEMPLATE = "Stones whisper. You choose not to listen. +%d XP."

        fun getAllTemplates() = listOf(AGGRO_MUSHROOM_TEMPLATE, MIMIC_TEMPLATE, STONES_WHISPER_TEMPLATE)
    }


    object TrinketMessages {
        const val CURIOUS_PEBBLE = "You find a curious pebble. It hums softly."
        const val FADED_RIBBON = "A faded ribbon flutters by—lucky?"
        const val SAFE_CAMPSITE = "You mark a safe campsite for later."

        fun getAllMessages() = listOf(CURIOUS_PEBBLE, FADED_RIBBON, SAFE_CAMPSITE)
    }
}
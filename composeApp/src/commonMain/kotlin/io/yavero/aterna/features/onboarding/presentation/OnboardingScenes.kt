package io.yavero.aterna.features.onboarding.presentation

enum class WarriorState { Idle, Walking, SwordReady }

data class Scene(
    val id: Int,
    val backgroundRes: Int,
    val message: String,
    val warriorState: WarriorState
)

object OnboardingScenes {
    private const val CAMP_BACKGROUND = 1001
    private const val PATH_BACKGROUND = 1002
    private const val DUNGEON_GATE_BACKGROUND = 1003
    private const val OPEN_GATE_BACKGROUND = 1004

    val scenes = listOf(
        Scene(
            id = 1,
            backgroundRes = CAMP_BACKGROUND,
            message = "Aterna helps you stay focused!",
            warriorState = WarriorState.Idle
        ),
        Scene(
            id = 2,
            backgroundRes = PATH_BACKGROUND,
            message = "Transform your boring tasks into quests.",
            warriorState = WarriorState.Walking
        ),
        Scene(
            id = 3,
            backgroundRes = DUNGEON_GATE_BACKGROUND,
            message = "Start a timer and earn rewards.",
            warriorState = WarriorState.SwordReady
        ),
        Scene(
            id = 4,
            backgroundRes = OPEN_GATE_BACKGROUND,
            message = "Pick your Class: Warrior for gold, Mage for XP.",
            warriorState = WarriorState.Idle
        )
    )
}
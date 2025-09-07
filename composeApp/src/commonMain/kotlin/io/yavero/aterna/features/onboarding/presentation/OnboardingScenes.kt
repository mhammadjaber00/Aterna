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
            message = "Welcome to Aterna — where focus becomes an adventure.",
            warriorState = WarriorState.Idle
        ),
        Scene(
            id = 2,
            backgroundRes = PATH_BACKGROUND,
            message = "Turn your everyday tasks into epic quests.",
            warriorState = WarriorState.Walking
        ),
        Scene(
            id = 3,
            backgroundRes = DUNGEON_GATE_BACKGROUND,
            message = "Start a quest timer and earn gold, XP, and loot.",
            warriorState = WarriorState.SwordReady
        ),
        Scene(
            id = 4,
            backgroundRes = OPEN_GATE_BACKGROUND,
            message = "Grow your adventurer’s Strength, Agility, Intelligence… and more.",
            warriorState = WarriorState.Idle
        )
    )
}
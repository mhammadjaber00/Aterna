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
            message = "Under a patient sky, Aterna calls its champions. Here, your mind is the map and your days the quest log. Ready yourself for the road ahead.",
            warriorState = WarriorState.Idle
        ),
        Scene(
            id = 2,
            backgroundRes = PATH_BACKGROUND,
            message = "Then came the Pale Fog—soft as sleep, greedy as time. Distractions steal focus and scatter goals. With discipline, the path clears beneath your feet.",
            warriorState = WarriorState.Walking
        ),
        Scene(
            id = 3,
            backgroundRes = DUNGEON_GATE_BACKGROUND,
            message = "Every finished task strikes at the chains. Each habit is a torch in the dark. Progress isn't given—it's earned, quest by quest.",
            warriorState = WarriorState.SwordReady
        ),
        Scene(
            id = 4,
            backgroundRes = OPEN_GATE_BACKGROUND,
            message = "Choose your Class. Warrior for gold, Mage for wisdom. Step through; the world will shape to your focus. Every quest sharpens the blade; every habit lights the way.",
            warriorState = WarriorState.Idle
        )
    )
}
package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState

data class OnboardingState(
    val currentSceneIndex: Int = 0,
    val isTransitioning: Boolean = false,
    val isWalkingAnimationPlaying: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: String? = null
) : MviState, LoadingState {

    val currentScene: Scene
        get() = OnboardingScenes.scenes[currentSceneIndex]

    val isLastScene: Boolean
        get() = currentSceneIndex == OnboardingScenes.scenes.size - 1

    val canProceed: Boolean
        get() = !isTransitioning && !isWalkingAnimationPlaying
}

data class OnboardingUiState(
    val currentScene: Scene,
    val isTransitioning: Boolean = false,
    val isWalkingAnimationPlaying: Boolean = false,
    val isLastScene: Boolean = false,
    val canProceed: Boolean = true
)

enum class WarriorState {
    Idle,
    Walking,
    SwordReady   
}

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
            message = "Under a patient sky, Aterna remembers the Evergloam— a crystal that once sharpened every mind and steadied every hand.",
            warriorState = WarriorState.Idle
        ),
        Scene(
            id = 2,
            backgroundRes = PATH_BACKGROUND,
            message = "Then came the Pale Fog. Soft as sleep, greedy as time. Days blurred; the crystal’s heart fell quiet.",
            warriorState = WarriorState.Walking
        ),
        Scene(
            id = 3,
            backgroundRes = DUNGEON_GATE_BACKGROUND,
            message = "Yet some still cut a path through the hush. Each focused task is a strike on the dungeon’s chains; each small win, a spark in Evergloam’s core.",
            warriorState = WarriorState.SwordReady
        ),
        Scene(
            id = 4,
            backgroundRes = OPEN_GATE_BACKGROUND,
            message = "Choose your discipline—Warrior or Mage—and step through. Every quest is a torch. Every habit, a blade.",
            warriorState = WarriorState.Idle
        )
    )
}
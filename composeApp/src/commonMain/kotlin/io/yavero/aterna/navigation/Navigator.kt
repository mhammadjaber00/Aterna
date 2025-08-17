package io.yavero.aterna.navigation

import io.yavero.aterna.domain.model.ClassType
import kotlinx.coroutines.flow.*

sealed interface Screen {
    data object Onboarding : Screen
    data object ClassSelect : Screen
    data object QuestHub : Screen
    data class Timer(
        val initialMinutes: Int = 25,
        val classType: ClassType = ClassType.WARRIOR
    ) : Screen
}

class Navigator {
    private val _stack = MutableStateFlow<List<Screen>>(emptyList())
    val stack: StateFlow<List<Screen>> = _stack.asStateFlow()

    // NEW: one-shot result bus to request starting a quest
    data class StartQuestRequest(val minutes: Int, val classType: ClassType)

    private val _pendingStartQuest = MutableSharedFlow<StartQuestRequest>(extraBufferCapacity = 1)
    val pendingStartQuest: SharedFlow<StartQuestRequest> = _pendingStartQuest.asSharedFlow()

    fun requestStartQuest(minutes: Int, classType: ClassType) {
        _pendingStartQuest.tryEmit(StartQuestRequest(minutes, classType))
    }

    val currentScreen: Screen?
        get() = _stack.value.lastOrNull()

    fun push(screen: Screen) {
        _stack.value = _stack.value + screen
    }

    fun replaceAll(screen: Screen) {
        _stack.value = listOf(screen)
    }

    fun bringToFront(screen: Screen) {
        val filtered = _stack.value.filterNot { it == screen }
        _stack.value = filtered + screen
    }

    fun pop(): Boolean {
        val cur = _stack.value
        return if (cur.size > 1) {
            _stack.value = cur.dropLast(1); true
        } else false
    }
}
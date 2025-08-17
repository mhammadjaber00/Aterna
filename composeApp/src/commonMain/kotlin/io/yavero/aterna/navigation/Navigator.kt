package io.yavero.aterna.navigation

import io.yavero.aterna.domain.model.ClassType
import kotlinx.coroutines.channels.Channel
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

data class StartQuestRequest(val minutes: Int, val classType: ClassType)

class Navigator {

    private val _stack = MutableStateFlow(listOf<Screen>(Screen.Onboarding))
    val stack: StateFlow<List<Screen>> = _stack.asStateFlow()

    private val startQuestBus = Channel<StartQuestRequest>(capacity = Channel.BUFFERED)
    val pendingStartQuest = startQuestBus.receiveAsFlow()

    fun push(screen: Screen) {
        _stack.update { it + screen }
    }

    fun pop() {
        _stack.update { if (it.size > 1) it.dropLast(1) else it }
    }

    fun replace(screen: Screen) {
        _stack.update { it.dropLast(1) + screen }
    }

    fun replaceAll(screen: Screen) {
        _stack.value = listOf(screen)
    }

    fun requestStartQuest(minutes: Int, classType: ClassType) {
        startQuestBus.trySend(StartQuestRequest(minutes, classType))
    }
}
package io.yavero.aterna.navigation

import io.yavero.aterna.domain.model.ClassType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

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
    val stack: StateFlow<List<Screen>> = _stack

    fun push(screen: Screen) = _stack.update { it + screen }
    fun replaceTop(screen: Screen) =
        _stack.update { if (it.isEmpty()) listOf(screen) else it.dropLast(1) + screen }

    fun replaceAll(screen: Screen) {
        _stack.value = listOf(screen)
    }
    fun pop(): Boolean {
        val cur = _stack.value
        if (cur.size <= 1) return false
        _stack.value = cur.dropLast(1)
        return true
    }

    fun navigateToOnboarding() = replaceAll(Screen.Onboarding)
    fun navigateToClassSelect() = replaceAll(Screen.ClassSelect)
    fun navigateToQuestHub() = replaceAll(Screen.QuestHub)
    fun navigateToTimer(minutes: Int, classType: ClassType) = push(Screen.Timer(minutes, classType))

    data class StartQuestRequest(val minutes: Int, val classType: ClassType)

    private val _pendingStartQuest = Channel<StartQuestRequest>(capacity = Channel.BUFFERED)
    val pendingStartQuest = _pendingStartQuest.receiveAsFlow()

    fun requestStartQuest(minutes: Int, classType: ClassType) {
        _pendingStartQuest.trySend(StartQuestRequest(minutes, classType))
    }
}
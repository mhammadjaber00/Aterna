package io.yavero.aterna.features.quest.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.repository.StatusEffectRepository
import io.yavero.aterna.domain.service.RewardService
import io.yavero.aterna.domain.util.RewardBankingStrategy
import io.yavero.aterna.features.quest.notification.QuestNotifier
import io.yavero.aterna.features.quest.presentation.QuestViewModel
import io.yavero.aterna.navigation.Navigator
import org.koin.compose.koinInject

@Composable
fun QuestRoute(
    onNavigateToTimer: (Int, ClassType) -> Unit,
) {
    val heroRepository = koinInject<HeroRepository>()
    val questRepository = koinInject<QuestRepository>()
    val questNotifier = koinInject<QuestNotifier>()
    val statusEffectRepository = koinInject<StatusEffectRepository>()
    val rewardService = koinInject<RewardService>()
    val bankingStrategy = koinInject<RewardBankingStrategy>()
    val navigator = koinInject<Navigator>()

    val vm = androidx.lifecycle.viewmodel.compose.viewModel(initializer = {
        QuestViewModel(
            heroRepository = heroRepository,
            questRepository = questRepository,
            questNotifier = questNotifier,
            statusEffectRepository = statusEffectRepository,
            rewardService = rewardService,
            bankingStrategy = bankingStrategy
        )
    })
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) {
        vm.effects.collect { effect ->
            when (effect) {
                is QuestViewModel.Effect.NavigateToTimer -> onNavigateToTimer(effect.initialMinutes, effect.classType)
                is QuestViewModel.Effect.ShowError -> {}
                is QuestViewModel.Effect.ShowMessage -> {}
            }
        }
    }

    LaunchedEffect(navigator) {
        navigator.pendingStartQuest.collect { req ->
            vm.send(QuestViewModel.Event.StartQuest(req.minutes, req.classType))
        }
    }

    QuestScreen(
        uiState = state,
        onStartQuest = { minutes, classType -> vm.send(QuestViewModel.Event.StartQuest(minutes, classType)) },
        onGiveUpQuest = { vm.send(QuestViewModel.Event.GiveUpQuest) },
        onCompleteQuest = { vm.send(QuestViewModel.Event.CompleteQuest) },
        onRefresh = { vm.send(QuestViewModel.Event.Refresh) },
        onClearError = { vm.send(QuestViewModel.Event.ClearError) },
        onNavigateToTimer = onNavigateToTimer,
        onLoadAdventureLog = { vm.send(QuestViewModel.Event.LoadAdventureLog) },
    )
}
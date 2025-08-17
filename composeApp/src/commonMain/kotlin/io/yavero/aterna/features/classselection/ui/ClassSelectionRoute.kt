package io.yavero.aterna.features.classselection.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.features.onboarding.classselect.presentation.ClassSelectionViewModel
import io.yavero.aterna.features.onboarding.classselect.ui.ClassSelectionScreen
import org.koin.compose.koinInject

@Composable
fun ClassSelectionRoute(
    onDone: () -> Unit,
) {
    val heroRepository = koinInject<HeroRepository>()
    val vm = viewModel(initializer = { ClassSelectionViewModel(heroRepository) })
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) {
        vm.effects.collect { effect ->
            when (effect) {
                ClassSelectionViewModel.Effect.NavigateToQuestHub -> onDone()
                is ClassSelectionViewModel.Effect.ShowError -> {
                    // Handle error display if needed
                }
            }
        }
    }

    ClassSelectionScreen(
        selected = state.selected,
        onSelect = { vm.send(ClassSelectionViewModel.Event.Select(it)) },
        onConfirm = { _ -> vm.send(ClassSelectionViewModel.Event.Confirm) },
    )
}

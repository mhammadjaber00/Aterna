package io.yavero.aterna.features.onboarding.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.repository.HeroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class DefaultClassSelectComponent(
    componentContext: ComponentContext,
    private val onNavigateToQuestHub: () -> Unit
) : ClassSelectComponent, ComponentContext by componentContext, KoinComponent {

    private val heroRepository: HeroRepository by inject()
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        lifecycle.doOnCreate {
            componentScope.launch {
                val existing = heroRepository.getCurrentHero()
                if (existing != null) {
                    onNavigateToQuestHub()
                }
            }
        }
    }

    override fun onClassSelected(classType: ClassType) {
        componentScope.launch {
            val existing = heroRepository.getCurrentHero()
            if (existing == null) {
                val hero = Hero(
                    id = Uuid.random().toString(),
                    name = "Hero",
                    classType = classType,
                    lastActiveDate = Clock.System.now()
                )
                heroRepository.insertHero(hero)
            } else if (existing.classType != classType) {
                heroRepository.updateHero(existing.copy(classType = classType, lastActiveDate = Clock.System.now()))
            }
            onNavigateToQuestHub()
        }
    }

    override fun onDismiss() {
        onNavigateToQuestHub()
    }
}
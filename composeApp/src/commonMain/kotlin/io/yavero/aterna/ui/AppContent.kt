package io.yavero.aterna.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.QuestType
import io.yavero.aterna.features.analytics.screen.AnalyticsScreen
import io.yavero.aterna.features.hero_stats.HeroStatsScreen
import io.yavero.aterna.features.inventory.InventoryScreen
import io.yavero.aterna.features.logbook.LogbookScreen
import io.yavero.aterna.features.onboarding.ui.OnboardingScreen
import io.yavero.aterna.features.quest.presentation.QuestComponent
import io.yavero.aterna.features.timer.TimerScreen
import io.yavero.aterna.navigation.AppRootComponent
import io.yavero.aterna.features.quest.screen.QuestScreen as FeatureQuestScreen

@Composable
fun AppContent(
    component: AppRootComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation {
                    fade(
                        animationSpec = tween(durationMillis = 240)
                    )
                }
            ) {
                when (val instance = it.instance) {
                    is AppRootComponent.Child.Onboarding -> OnboardingScreen(component = instance.component)
//                    is AppRootComponent.Child.ClassSelect -> ClassSelectScreen(component = instance.component)
                    is AppRootComponent.Child.QuestHub -> QuestHubScreen(component = instance.component)
                    is AppRootComponent.Child.Inventory -> InventoryScreen(component = instance.component)
                    is AppRootComponent.Child.Timer -> TimerScreenWrapper(
                        initialMinutes = instance.initialMinutes,
                        classType = instance.classType,
                        component = component
                    )
                    is AppRootComponent.Child.HeroStats -> HeroStatsScreen(component = instance.component)
                    is AppRootComponent.Child.Analytics -> AnalyticsScreen(component = instance.component)
                    is AppRootComponent.Child.Logbook -> LogbookScreen(component = instance.component)
                }
            }
        }
    }
}

//@Composable
//private fun ClassSelectScreen(component: ClassSelectComponent) {
//    var selectedClass by remember { mutableStateOf<ClassType?>(null) }
//
//    ClassSelectionScreen(
//        selected = selectedClass,
//        onSelect = { selectedClass = it },
//        onConfirm = { classType ->
//            component.onClassSelected(classType)
//        },
//    )
//}

@Composable
private fun QuestHubScreen(component: QuestComponent) {
    FeatureQuestScreen(component = component)
}

@Composable
private fun TimerScreenWrapper(
    initialMinutes: Int,
    classType: String,
    component: AppRootComponent
) {
    val classTypeEnum = try {
        ClassType.valueOf(classType)
    } catch (e: IllegalArgumentException) {
        ClassType.ADVENTURER
    }

    TimerScreen(
        initialMinutes = initialMinutes,
        classType = classTypeEnum,
        onConfirm = { duration: Int, questType: QuestType ->
            component.startQuest(duration, classType, questType)
        },
        onDismiss = {
            component.navigateToQuestHub()
        }
    )
}

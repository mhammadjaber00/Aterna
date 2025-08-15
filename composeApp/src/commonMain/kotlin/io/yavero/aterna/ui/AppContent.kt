package io.yavero.aterna.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.features.onboarding.ui.ClassSelectComponent
import io.yavero.aterna.features.onboarding.ui.OnboardingScreen
import io.yavero.aterna.features.quest.TimerScreen
import io.yavero.aterna.features.quest.component.QuestComponent
import io.yavero.aterna.features.quest.select.ClassSelectionScreen
import io.yavero.aterna.navigation.AppRootComponent
import io.yavero.aterna.features.quest.QuestScreen as FeatureQuestScreen

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
                    is AppRootComponent.Child.ClassSelect -> ClassSelectScreen(component = instance.component)
                    is AppRootComponent.Child.QuestHub -> QuestHubScreen(component = instance.component)
                    is AppRootComponent.Child.Timer -> TimerScreenWrapper(
                        initialMinutes = instance.initialMinutes,
                        classType = instance.classType,
                        component = component
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassSelectScreen(component: ClassSelectComponent) {
    var selectedClass by remember { mutableStateOf<ClassType?>(null) }

    ClassSelectionScreen(
        selected = selectedClass,
        onSelect = { selectedClass = it },
        onConfirm = { classType ->
            component.onClassSelected(classType)
        },
    )
}

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
        ClassType.WARRIOR
    }

    TimerScreen(
        initialMinutes = initialMinutes,
        classType = classTypeEnum,
        onConfirm = { duration: Int ->
            component.startQuest(duration, classType)
        },
        onDismiss = {
            component.navigateToQuestHub()
        }
    )
}

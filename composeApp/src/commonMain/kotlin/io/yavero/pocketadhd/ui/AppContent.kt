package io.yavero.pocketadhd.ui

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
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.feature.onboarding.ui.ClassSelectComponent
import io.yavero.pocketadhd.feature.onboarding.ui.OnboardingScreen
import io.yavero.pocketadhd.feature.quest.component.QuestComponent
import io.yavero.pocketadhd.feature.quest.select.ClassSelectionScreen
import io.yavero.pocketadhd.navigation.AppRootComponent
import io.yavero.pocketadhd.feature.quest.QuestScreen as FeatureQuestScreen

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

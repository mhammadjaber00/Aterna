package io.yavero.aterna

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import io.yavero.aterna.features.quest.notification.QuestActions
import io.yavero.aterna.features.quest.presentation.QuestIntent
import io.yavero.aterna.features.quest.presentation.QuestStore
import io.yavero.aterna.navigation.DefaultAppRootComponent
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val questStore: QuestStore by inject()

    private lateinit var rootComponent: DefaultAppRootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        rootComponent = DefaultAppRootComponent(
            componentContext = defaultComponentContext()
        )

        setContent { App(rootComponent = rootComponent) }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra(QuestActions.EXTRA_ACTION_TYPE)
            ?: intent?.action
            ?: return

        when (action) {
            QuestActions.ACTION_VIEW_LOGS -> {
                questStore.process(QuestIntent.RequestShowAdventureLog)
            }

            QuestActions.ACTION_RETREAT -> {
                questStore.process(QuestIntent.RequestRetreatConfirm)
            }
        }
    }
}
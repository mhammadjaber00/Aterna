package io.yavero.aterna.features.quest.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.aterna.features.quest.presentation.QuestIntent
import io.yavero.aterna.features.quest.presentation.QuestStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RetreatPromptActivity : ComponentActivity(), KoinComponent {

    private val questStore: QuestStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Confirmation(onConfirm = {
                        questStore.process(QuestIntent.GiveUp)
                        finish()
                    }, onCancel = {
                        finish()
                    })
                }
            }
        }
    }
}

@Composable
private fun Confirmation(onConfirm: () -> Unit, onCancel: () -> Unit) {
    val open = remember { mutableStateOf(true) }
    if (open.value) {
        AlertDialog(
            onDismissRequest = { open.value = false; onCancel() },
            title = { Text("Retreat from Quest?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Retreating early will incur a curse that reduces XP and gold gains for a short time.")
                    Text("You may bank partial rewards from the time already spent.")
                }
            },
            confirmButton = {
                Button(onClick = { open.value = false; onConfirm() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Retreat")
                }
            },
            dismissButton = {
                Button(onClick = { open.value = false; onCancel() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        )
    }
}
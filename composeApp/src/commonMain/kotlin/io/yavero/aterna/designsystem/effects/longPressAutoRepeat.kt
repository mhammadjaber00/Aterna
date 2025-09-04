package io.yavero.aterna.designsystem.effects

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.longPressAutoRepeat(
    initialDelayMillis: Long = 350,
    repeatDelayMillis: Long = 80,
    markSkipClick: (Boolean) -> Unit,
    onRepeat: () -> Unit
): Modifier = composed {
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            markSkipClick(false)
            val job = CoroutineScope(Dispatchers.Default).launch {
                delay(initialDelayMillis)
                markSkipClick(true)
                while (true) {
                    onRepeat()
                    delay(repeatDelayMillis)
                }
            }
            waitForUpOrCancellation()
            job.cancel()
        }
    }
}

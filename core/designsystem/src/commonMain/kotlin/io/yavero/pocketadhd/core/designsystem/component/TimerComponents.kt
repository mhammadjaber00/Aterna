package io.yavero.pocketadhd.core.designsystem.component

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

fun formatTime(milliseconds: Long, showMilliseconds: Boolean): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ms = (milliseconds % 1000) / 10

    return if (showMilliseconds && totalSeconds < 60) {
        "${seconds.toString().padStart(2, '0')}.${ms.toString().padStart(2, '0')}"
    } else {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}
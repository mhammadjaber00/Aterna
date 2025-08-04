package io.yavero.pocketadhd.feature.planner.model

import io.yavero.pocketadhd.core.domain.model.Task

/**
 * UI model for tasks that includes UI-specific state like expanded and selected flags
 */
data class TaskUiModel(
    val task: Task,
    val isExpanded: Boolean = false,
    val isSelected: Boolean = false
)

/**
 * Data class for snackbar state
 */
data class SnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)
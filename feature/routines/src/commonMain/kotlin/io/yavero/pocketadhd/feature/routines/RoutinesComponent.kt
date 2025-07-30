package io.yavero.pocketadhd.feature.routines

import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.feature.routines.presentation.RoutinesState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalTime

/**
 * Routines component for managing daily routines
 * 
 * Features:
 * - Morning, evening, and hygiene routines
 * - Step-by-step execution with timers
 * - Progress tracking and completion
 * - Routine customization and scheduling
 * - Habit building and streaks
 */
interface RoutinesComponent {
    val uiState: StateFlow<RoutinesState>
    
    fun onStartRoutine(routineId: String)
    fun onPauseRoutine()
    fun onResumeRoutine()
    fun onCompleteStep()
    fun onSkipStep()
    fun onCompleteRoutine()
    fun onCancelRoutine()
    fun onCreateRoutine()
    fun onEditRoutine(routineId: String)
    fun onDeleteRoutine(routineId: String)
    fun onToggleRoutineActive(routineId: String)
    fun onRefresh()
}

data class RoutinesUiState(
    val isLoading: Boolean = false,
    val routines: List<Routine> = emptyList(),
    val activeRoutineExecution: RoutineExecution? = null,
    val todayStats: RoutineStats = RoutineStats(),
    val error: String? = null
)

data class RoutineExecution(
    val routine: Routine,
    val currentStepIndex: Int = 0,
    val stepStartTime: kotlinx.datetime.Instant? = null,
    val completedSteps: Set<String> = emptySet(),
    val skippedSteps: Set<String> = emptySet(),
    val state: RoutineExecutionState = RoutineExecutionState.READY,
    val totalElapsedSeconds: Long = 0
)

enum class RoutineExecutionState {
    READY,
    RUNNING,
    PAUSED,
    STEP_COMPLETED,
    ROUTINE_COMPLETED,
    CANCELLED
}

data class RoutineStats(
    val completedRoutines: Int = 0,
    val totalStepsCompleted: Int = 0,
    val averageCompletionTime: Int = 0, // in minutes
    val currentStreak: Int = 0,
    val completionRate: Float = 0f
)

/**
 * Routine editor component for creating and editing routines
 */
interface RoutineEditorComponent {
    val uiState: StateFlow<RoutineEditorUiState>
    
    fun onNameChanged(name: String)
    fun onStepAdded(title: String, durationSeconds: Int?, icon: String?)
    fun onStepRemoved(stepId: String)
    fun onStepUpdated(stepId: String, title: String, durationSeconds: Int?, icon: String?)
    fun onStepReordered(fromIndex: Int, toIndex: Int)
    fun onScheduleChanged(schedule: RoutineScheduleData?)
    fun onActiveChanged(isActive: Boolean)
    fun onSave()
    fun onCancel()
}

data class RoutineEditorUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val routineId: String? = null,
    val name: String = "",
    val steps: List<RoutineStepItem> = emptyList(),
    val schedule: RoutineScheduleData? = null,
    val isActive: Boolean = true,
    val canSave: Boolean = false,
    val error: String? = null
)

data class RoutineStepItem(
    val id: String,
    val title: String,
    val durationSeconds: Int? = null,
    val icon: String? = null,
    val order: Int = 0
)

data class RoutineScheduleData(
    val daysOfWeek: List<Int> = emptyList(), // 1..7 (Monday = 1, Sunday = 7)
    val times: List<LocalTime> = emptyList()
)

/**
 * Routine templates for common routines
 */
object RoutineTemplates {
    val MORNING_ROUTINE = RoutineTemplate(
        name = "Morning Routine",
        steps = listOf(
            RoutineStepTemplate("Wake up and stretch", 300, "🧘"),
            RoutineStepTemplate("Brush teeth", 120, "🦷"),
            RoutineStepTemplate("Shower", 600, "🚿"),
            RoutineStepTemplate("Get dressed", 300, "👕"),
            RoutineStepTemplate("Eat breakfast", 900, "🍳"),
            RoutineStepTemplate("Review daily goals", 300, "📝")
        )
    )
    
    val EVENING_ROUTINE = RoutineTemplate(
        name = "Evening Routine",
        steps = listOf(
            RoutineStepTemplate("Tidy up workspace", 600, "🧹"),
            RoutineStepTemplate("Plan tomorrow", 300, "📅"),
            RoutineStepTemplate("Brush teeth", 120, "🦷"),
            RoutineStepTemplate("Skincare routine", 300, "🧴"),
            RoutineStepTemplate("Read or journal", 900, "📖"),
            RoutineStepTemplate("Prepare for sleep", 300, "😴")
        )
    )
    
    val HYGIENE_ROUTINE = RoutineTemplate(
        name = "Hygiene Routine",
        steps = listOf(
            RoutineStepTemplate("Brush teeth", 120, "🦷"),
            RoutineStepTemplate("Floss", 60, "🦷"),
            RoutineStepTemplate("Mouthwash", 30, "🧴"),
            RoutineStepTemplate("Wash face", 120, "🧼"),
            RoutineStepTemplate("Apply moisturizer", 60, "🧴")
        )
    )
}

data class RoutineTemplate(
    val name: String,
    val steps: List<RoutineStepTemplate>
)

data class RoutineStepTemplate(
    val title: String,
    val durationSeconds: Int,
    val icon: String
)

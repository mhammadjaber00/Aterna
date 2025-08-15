package io.yavero.pocketadhd.domain.error

sealed class AppError : Exception() {

    sealed class Network : AppError() {
        data object NoConnection : Network()
        data object Timeout : Network()
        data class HttpError(val code: Int, override val message: String) : Network()
        data class Unknown(override val message: String) : Network()
    }

    sealed class Database : AppError() {
        data object NotFound : Database()
        data class ConstraintViolation(override val message: String) : Database()
        data class Corruption(override val message: String) : Database()
        data class Unknown(override val message: String) : Database()
    }

    sealed class Validation : AppError() {
        data class InvalidInput(val field: String, override val message: String) : Validation()
        data class MissingRequired(val field: String) : Validation()
        data class OutOfRange(val field: String, val min: Any?, val max: Any?) : Validation()
    }

    sealed class Security : AppError() {
        data object Unauthorized : Security()
        data object Forbidden : Security()
        data object EncryptionFailed : Security()
        data object DecryptionFailed : Security()
    }

    sealed class Business : AppError() {
        data object TaskAlreadyCompleted : Business()
        data object FocusSessionInProgress : Business()
        data object RoutineNotActive : Business()
        data object MoodEntryAlreadyExists : Business()
        data object MedicationNotDue : Business()
        data class Unknown(override val message: String) : Business()
    }

    sealed class System : AppError() {
        data object OutOfMemory : System()
        data object StorageFull : System()
        data object PermissionDenied : System()
        data class Unknown(override val message: String) : System()
    }

    data class Unknown(override val message: String) : AppError()
}

fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        is IllegalArgumentException -> AppError.Validation.InvalidInput("unknown", message ?: "Invalid input")
        is IllegalStateException -> AppError.Business.Unknown(message ?: "Invalid state")
        else -> AppError.Unknown(message ?: "Unknown error occurred")
    }
}

fun AppError.getUserMessage(): String {
    return when (this) {

        is AppError.Network.NoConnection -> "No internet connection available"
        is AppError.Network.Timeout -> "Request timed out. Please try again"
        is AppError.Network.HttpError -> "Server error: $message"
        is AppError.Network.Unknown -> "Network error: $message"


        is AppError.Database.NotFound -> "Data not found"
        is AppError.Database.ConstraintViolation -> "Data validation failed"
        is AppError.Database.Corruption -> "Data corruption detected. Please restart the app"
        is AppError.Database.Unknown -> "Database error: $message"


        is AppError.Validation.InvalidInput -> "Invalid $field: $message"
        is AppError.Validation.MissingRequired -> "$field is required"
        is AppError.Validation.OutOfRange -> "$field is out of range"


        is AppError.Security.Unauthorized -> "Access denied"
        is AppError.Security.Forbidden -> "Operation not allowed"
        is AppError.Security.EncryptionFailed -> "Failed to secure data"
        is AppError.Security.DecryptionFailed -> "Failed to access secure data"


        is AppError.Business.TaskAlreadyCompleted -> "Task is already completed"
        is AppError.Business.FocusSessionInProgress -> "A focus session is already in progress"
        is AppError.Business.RoutineNotActive -> "Routine is not currently active"
        is AppError.Business.MoodEntryAlreadyExists -> "Mood entry already exists for today"
        is AppError.Business.MedicationNotDue -> "Medication is not due yet"
        is AppError.Business.Unknown -> "Business logic error: $message"


        is AppError.System.OutOfMemory -> "Not enough memory available"
        is AppError.System.StorageFull -> "Storage is full"
        is AppError.System.PermissionDenied -> "Permission denied"
        is AppError.System.Unknown -> "System error: $message"


        is AppError.Unknown -> message
    }
}
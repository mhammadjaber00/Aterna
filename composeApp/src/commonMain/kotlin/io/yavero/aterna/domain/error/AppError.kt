package io.yavero.aterna.domain.error

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
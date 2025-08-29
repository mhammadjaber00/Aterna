package io.yavero.aterna.domain.model

data class ValidationResult(val valid: Boolean, val reason: String? = null)
package io.yavero.aterna.features.onboarding.ui

import io.yavero.aterna.domain.model.ClassType

interface ClassSelectComponent {
    fun onClassSelected(classType: ClassType)
    fun onDismiss()
}
package io.yavero.pocketadhd.features.onboarding.ui

import io.yavero.pocketadhd.domain.model.ClassType

interface ClassSelectComponent {
    fun onClassSelected(classType: ClassType)
    fun onDismiss()
}
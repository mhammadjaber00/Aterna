package io.yavero.pocketadhd.feature.onboarding.ui

import io.yavero.pocketadhd.core.domain.model.ClassType

interface ClassSelectComponent {
    fun onClassSelected(classType: ClassType)
    fun onDismiss()
}
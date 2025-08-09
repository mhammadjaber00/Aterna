package io.yavero.pocketadhd.feature.onboarding.ui

import com.arkivanov.decompose.ComponentContext
import io.yavero.pocketadhd.core.domain.model.ClassType

class DefaultClassSelectComponent(
    componentContext: ComponentContext,
    private val onNavigateToQuestHub: () -> Unit
) : ClassSelectComponent, ComponentContext by componentContext {

    override fun onClassSelected(classType: ClassType) {


        onNavigateToQuestHub()
    }

    override fun onDismiss() {

        onNavigateToQuestHub()
    }
}
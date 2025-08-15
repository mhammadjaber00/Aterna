package io.yavero.pocketadhd.features.onboarding.ui

import com.arkivanov.decompose.ComponentContext
import io.yavero.pocketadhd.domain.model.ClassType

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
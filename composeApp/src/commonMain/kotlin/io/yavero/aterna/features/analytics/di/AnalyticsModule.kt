package io.yavero.aterna.features.analytics.di

import com.arkivanov.decompose.ComponentContext
import io.yavero.aterna.features.analytics.presentation.DefaultAnalyticsComponent
import org.koin.dsl.module

val analyticsModule = module {
    factory { (ctx: ComponentContext, onBack: () -> Unit) ->
        DefaultAnalyticsComponent(
            componentContext = ctx,
            heroRepository = get(),
            questRepository = get(),
            timeProvider = get(),
            onBackNav = onBack
        )
    }
}
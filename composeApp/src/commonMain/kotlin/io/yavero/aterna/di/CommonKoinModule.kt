package io.yavero.aterna.di

import io.yavero.aterna.data.di.dataModule
import io.yavero.aterna.domain.util.TimeProvider
import io.yavero.aterna.features.onboarding.di.onboardingModule
import io.yavero.aterna.features.quest.di.focusModule
import io.yavero.aterna.notifications.di.notificationsModule
import io.yavero.aterna.services.time.DefaultTimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single<TimeProvider> { DefaultTimeProvider() }
}

fun getCommonKoinModules(): List<Module> = listOf(
    commonModule,
    dataModule,
    notificationsModule,
    focusModule,
    onboardingModule,
)
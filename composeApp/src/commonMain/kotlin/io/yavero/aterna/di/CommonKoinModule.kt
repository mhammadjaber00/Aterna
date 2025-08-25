package io.yavero.aterna.di

import io.yavero.aterna.data.di.dataModule
import io.yavero.aterna.features.onboarding.di.onboardingModule
import io.yavero.aterna.features.quest.di.focusModule
import io.yavero.aterna.notifications.di.notificationsModule
import org.koin.core.module.Module

fun getCommonKoinModules(): List<Module> = listOf(
    dataModule,
    notificationsModule,
    focusModule,
    onboardingModule,
)
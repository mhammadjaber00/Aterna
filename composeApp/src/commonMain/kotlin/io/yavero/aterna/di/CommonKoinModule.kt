package io.yavero.aterna.di

import io.yavero.aterna.data.di.dataModule
import io.yavero.aterna.domain.service.TaskNotificationService
import io.yavero.aterna.domain.service.TaskNotificationServiceImpl
import io.yavero.aterna.features.onboarding.di.onboardingModule
import io.yavero.aterna.features.quest.di.focusModule
import io.yavero.aterna.notifications.di.notificationsModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    singleOf(::TaskNotificationServiceImpl) bind TaskNotificationService::class
}

val viewModelsModule = module {
    // TODO: Add ViewModels when they are created
}

fun getCommonKoinModules(): List<Module> = listOf(
    dataModule,
    domainModule,
    notificationsModule,
    focusModule,
    onboardingModule,
    viewModelsModule
)
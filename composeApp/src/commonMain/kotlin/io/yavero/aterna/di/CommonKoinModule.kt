package io.yavero.aterna.di

import io.yavero.aterna.data.di.dataModule
import io.yavero.aterna.domain.service.TaskNotificationService
import io.yavero.aterna.domain.service.TaskNotificationServiceImpl
import io.yavero.aterna.navigation.Navigator
import io.yavero.aterna.navigation.RootViewModel
import io.yavero.aterna.notifications.di.notificationsModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    singleOf(::TaskNotificationServiceImpl) bind TaskNotificationService::class
}

val navigationModule = module {
    single { Navigator() }
    single {
        RootViewModel(
            navigator = get(),
            heroRepository = get(),
            settingsRepository = get()
        )
    }
}

fun getCommonKoinModules(): List<Module> = listOf(
    dataModule,
    domainModule,
    navigationModule,
    notificationsModule,
)
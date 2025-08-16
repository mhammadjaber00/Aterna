package io.yavero.aterna.data.di

import com.russhwolf.settings.Settings
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.DatabaseDriverFactory
import io.yavero.aterna.data.remote.QuestApi
import io.yavero.aterna.data.remote.createMockQuestApi
import io.yavero.aterna.data.repository.*
import io.yavero.aterna.domain.repository.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {

    single { Settings() }


    single<AternaDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        AternaDatabase(driverFactory.createDriver())
    }


    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class

    single<TaskRepository> {
        TaskRepositoryImpl(
            database = get<AternaDatabase>(),
            taskNotificationService = get()
        )
    }
    singleOf(::FocusSessionRepositoryImpl) bind FocusSessionRepository::class
    singleOf(::HeroRepositoryImpl) bind HeroRepository::class
    singleOf(::QuestRepositoryImpl) bind QuestRepository::class


    single<QuestApi> { createMockQuestApi() }
}
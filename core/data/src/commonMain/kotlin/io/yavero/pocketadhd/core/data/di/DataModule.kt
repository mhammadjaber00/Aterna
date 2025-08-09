package io.yavero.pocketadhd.core.data.di

import com.russhwolf.settings.Settings
import io.yavero.pocketadhd.core.data.database.DatabaseDriverFactory
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.data.remote.QuestApi
import io.yavero.pocketadhd.core.data.remote.createMockQuestApi
import io.yavero.pocketadhd.core.data.repository.*
import io.yavero.pocketadhd.core.domain.repository.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {

    single { Settings() }


    single<PocketAdhdDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        PocketAdhdDatabase(driverFactory.createDriver())
    }


    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    
    single<TaskRepository> { 
        TaskRepositoryImpl(
            database = get<PocketAdhdDatabase>(),
            localNotifier = get()
        )
    }
    singleOf(::RoutineRepositoryImpl) bind RoutineRepository::class
    singleOf(::FocusSessionRepositoryImpl) bind FocusSessionRepository::class
    singleOf(::MoodEntryRepositoryImpl) bind MoodEntryRepository::class
    singleOf(::MedicationRepositoryImpl) bind MedicationRepository::class
    singleOf(::GameResultRepositoryImpl) bind GameResultRepository::class
    singleOf(::HeroRepositoryImpl) bind HeroRepository::class
    singleOf(::QuestRepositoryImpl) bind QuestRepository::class


    single<QuestApi> { createMockQuestApi() }
}
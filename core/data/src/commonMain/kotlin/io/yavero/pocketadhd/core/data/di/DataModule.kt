package io.yavero.pocketadhd.core.data.di

import com.russhwolf.settings.Settings
import io.yavero.pocketadhd.core.data.database.DatabaseDriverFactory
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.data.repository.FocusSessionRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.GameResultRepositoryImpl
import io.yavero.pocketadhd.core.data.remote.QuestApi
import io.yavero.pocketadhd.core.data.remote.createMockQuestApi
import io.yavero.pocketadhd.core.data.repository.HeroRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.MedicationRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.MoodEntryRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.QuestRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.RoutineRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.SettingsRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.TaskRepositoryImpl
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.repository.GameResultRepository
import io.yavero.pocketadhd.core.domain.repository.HeroRepository
import io.yavero.pocketadhd.core.domain.repository.MedicationRepository
import io.yavero.pocketadhd.core.domain.repository.MoodEntryRepository
import io.yavero.pocketadhd.core.domain.repository.QuestRepository
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import io.yavero.pocketadhd.core.domain.repository.SettingsRepository
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Settings
    single { Settings() }
    
    // Database
    single<PocketAdhdDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        PocketAdhdDatabase(driverFactory.createDriver())
    }
    
    // Repositories
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

    // Quest API (mock for now, can be replaced with real implementation later)
    single<QuestApi> { createMockQuestApi() }
}
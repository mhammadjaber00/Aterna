package io.yavero.pocketadhd.core.data.di

import io.yavero.pocketadhd.core.data.database.DatabaseDriverFactory
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.data.repository.FocusSessionRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.GameResultRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.MedicationRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.MoodEntryRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.RoutineRepositoryImpl
import io.yavero.pocketadhd.core.data.repository.TaskRepositoryImpl
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.repository.GameResultRepository
import io.yavero.pocketadhd.core.domain.repository.MedicationRepository
import io.yavero.pocketadhd.core.domain.repository.MoodEntryRepository
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Database
    single<PocketAdhdDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        PocketAdhdDatabase(driverFactory.createDriver())
    }
    
    // Repositories
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
    singleOf(::RoutineRepositoryImpl) bind RoutineRepository::class
    singleOf(::FocusSessionRepositoryImpl) bind FocusSessionRepository::class
    singleOf(::MoodEntryRepositoryImpl) bind MoodEntryRepository::class
    singleOf(::MedicationRepositoryImpl) bind MedicationRepository::class
    singleOf(::GameResultRepositoryImpl) bind GameResultRepository::class
}
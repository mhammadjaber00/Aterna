package io.yavero.aterna.data.di

import com.russhwolf.settings.Settings
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.DatabaseDriverFactory
import io.yavero.aterna.data.remote.QuestApi
import io.yavero.aterna.data.remote.createMockQuestApi
import io.yavero.aterna.data.repository.*
import io.yavero.aterna.domain.repository.*
import io.yavero.aterna.domain.service.quest.RewardService
import io.yavero.aterna.domain.util.FixedIntervalBankingStrategy
import io.yavero.aterna.domain.util.RealTimeProvider
import io.yavero.aterna.domain.util.RewardBankingStrategy
import io.yavero.aterna.domain.util.TimeProvider
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
    singleOf(::InventoryRepositoryImpl) bind InventoryRepository::class

    singleOf(::RealTimeProvider) bind TimeProvider::class
    single<StatusEffectRepository> {
        StatusEffectRepositoryImpl(
            database = get<AternaDatabase>(),
            timeProvider = get<TimeProvider>()
        )
    }

    single<RewardBankingStrategy> { FixedIntervalBankingStrategy() }
    singleOf(::RewardService)

    single<QuestApi> { createMockQuestApi() }
}
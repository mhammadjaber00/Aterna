package io.yavero.aterna.data.di

import com.russhwolf.settings.Settings
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.DatabaseDriverFactory
import io.yavero.aterna.data.remote.QuestApi
import io.yavero.aterna.data.remote.createMockQuestApi
import io.yavero.aterna.data.repository.*
import io.yavero.aterna.domain.quest.economy.RewardService
import io.yavero.aterna.domain.repository.*
import io.yavero.aterna.domain.service.attr.AttributeProgressService
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
    singleOf(::HeroRepositoryImpl) bind HeroRepository::class
    singleOf(::QuestRepositoryImpl) bind QuestRepository::class
    singleOf(::InventoryRepositoryImpl) bind InventoryRepository::class

    single<StatusEffectRepository> {
        StatusEffectRepositoryImpl(
            database = get<AternaDatabase>(),
            timeProvider = get(),
            heroRepository = get()
        )
    }

    single<AttributeProgressRepository> { AttributeProgressRepositoryImpl(get()) }

    single { AttributeProgressService(attrRepo = get(), heroRepo = get(), questRepo = get()) }

    singleOf(::RewardService)

    single<QuestApi> { createMockQuestApi() }
}
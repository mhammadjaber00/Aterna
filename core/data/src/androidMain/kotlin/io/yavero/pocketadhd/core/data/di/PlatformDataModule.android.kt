package io.yavero.pocketadhd.core.data.di

import android.content.Context
import io.yavero.pocketadhd.core.data.database.DatabaseDriverFactory
import org.koin.dsl.module

val platformDataModule = module {
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory(context = get<Context>()) 
    }
}
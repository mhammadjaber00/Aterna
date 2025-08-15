package io.yavero.aterna.data.di

import android.content.Context
import io.yavero.aterna.data.database.DatabaseDriverFactory
import io.yavero.aterna.data.security.KeyManager
import org.koin.dsl.module

val platformDataModule = module {
    single<KeyManager> { 
        KeyManager(context = get<Context>()) 
    }
    
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory(
            context = get<Context>(),
            keyManager = get<KeyManager>()
        ) 
    }
}
package io.yavero.aterna.data.di

import io.yavero.aterna.data.database.DatabaseDriverFactory
import io.yavero.aterna.data.security.KeyManager
import org.koin.dsl.module

val platformDataModule = module {
    single<KeyManager> { 
        KeyManager() 
    }
    
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory(keyManager = get<KeyManager>()) 
    }
}
package io.yavero.pocketadhd.data.di

import io.yavero.pocketadhd.data.database.DatabaseDriverFactory
import io.yavero.pocketadhd.data.security.KeyManager
import org.koin.dsl.module

val platformDataModule = module {
    single<KeyManager> { 
        KeyManager() 
    }
    
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory(keyManager = get<KeyManager>()) 
    }
}
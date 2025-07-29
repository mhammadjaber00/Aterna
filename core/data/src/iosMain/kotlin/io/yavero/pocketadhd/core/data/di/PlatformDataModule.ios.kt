package io.yavero.pocketadhd.core.data.di

import io.yavero.pocketadhd.core.data.database.DatabaseDriverFactory
import io.yavero.pocketadhd.core.data.security.KeyManager
import org.koin.dsl.module

val platformDataModule = module {
    single<KeyManager> { 
        KeyManager() 
    }
    
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory(keyManager = get<KeyManager>()) 
    }
}
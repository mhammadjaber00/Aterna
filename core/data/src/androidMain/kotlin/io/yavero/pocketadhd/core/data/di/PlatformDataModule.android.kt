package io.yavero.pocketadhd.core.data.di

import android.content.Context
import io.yavero.pocketadhd.core.data.database.DatabaseDriverFactory
import io.yavero.pocketadhd.core.data.security.KeyManager
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
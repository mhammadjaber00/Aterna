package io.yavero.pocketadhd.core.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.yavero.pocketadhd.core.data.security.KeyManager
import kotlinx.coroutines.runBlocking

actual class DatabaseDriverFactory(
    private val keyManager: KeyManager
) {
    actual fun createDriver(): SqlDriver {


        runBlocking { keyManager.getOrCreateDbKey() }
        
        return NativeSqliteDriver(
            schema = PocketAdhdDatabase.Schema,
            name = "pocketadhd.db"
        )
    }
}
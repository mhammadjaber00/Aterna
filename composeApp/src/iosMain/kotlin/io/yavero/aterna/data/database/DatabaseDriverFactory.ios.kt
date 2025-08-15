package io.yavero.aterna.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.yavero.aterna.data.security.KeyManager
import kotlinx.coroutines.runBlocking

actual class DatabaseDriverFactory(
    private val keyManager: KeyManager
) {
    actual fun createDriver(): SqlDriver {


        runBlocking { keyManager.getOrCreateDbKey() }
        
        return NativeSqliteDriver(
            schema = AternaDatabase.Schema,
            name = "aterna.db"
        )
    }
}
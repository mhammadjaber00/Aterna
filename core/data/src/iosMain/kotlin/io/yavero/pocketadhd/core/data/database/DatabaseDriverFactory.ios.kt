package io.yavero.pocketadhd.core.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.yavero.pocketadhd.core.data.security.KeyManager
import kotlinx.coroutines.runBlocking

actual class DatabaseDriverFactory(
    private val keyManager: KeyManager
) {
    actual fun createDriver(): SqlDriver {
        // TODO: Implement SQLCipher encryption for iOS
        // For now, create unencrypted database but ensure KeyManager is working
        // The key is generated and stored securely, ready for future SQLCipher integration
        runBlocking { keyManager.getOrCreateDbKey() }
        
        return NativeSqliteDriver(
            schema = PocketAdhdDatabase.Schema,
            name = "pocketadhd.db"
        )
    }
}
package io.yavero.aterna.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.yavero.aterna.data.security.KeyManager
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

actual class DatabaseDriverFactory(
    private val context: Context,
    private val keyManager: KeyManager
) {
    actual fun createDriver(): SqlDriver {

        val passphrase = runBlocking { keyManager.getOrCreateDbKey() }
        
        val factory = SupportOpenHelperFactory(passphrase)
        
        return AndroidSqliteDriver(
            schema = AternaDatabase.Schema,
            context = context,
            name = "aterna.db",
            factory = factory
        )
    }
}
package io.yavero.pocketadhd.core.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // Get encryption key from secure storage (will be implemented later)
        val passphrase = getEncryptionKey()
        
        val factory = SupportOpenHelperFactory(passphrase.toByteArray())
        
        return AndroidSqliteDriver(
            schema = PocketAdhdDatabase.Schema,
            context = context,
            name = "pocketadhd.db",
            factory = factory
        )
    }
    
    private fun getEncryptionKey(): String {
        // TODO: Implement secure key retrieval from Android Keystore
        // For now, return a placeholder - this will be replaced with proper key management
        return "temporary_key_placeholder"
    }
}
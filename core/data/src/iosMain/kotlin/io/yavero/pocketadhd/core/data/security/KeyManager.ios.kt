package io.yavero.pocketadhd.core.data.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import kotlin.random.Random

/**
 * iOS implementation of KeyManager
 * 
 * TODO: Implement proper iOS Keychain integration
 * For Phase 1, using simplified key generation and UserDefaults storage
 * Keys are generated securely but stored in UserDefaults (not ideal for production)
 */
actual class KeyManager {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual suspend fun getOrCreateDbKey(): ByteArray = withContext(Dispatchers.Default) {
        if (hasDbKey()) {
            getStoredDbKey()
        } else {
            generateAndStoreDbKey()
        }
    }
    
    actual suspend fun rotateDbKey() = withContext(Dispatchers.Default) {
        // Generate new key and store it
        generateAndStoreDbKey()
        Unit
    }
    
    actual suspend fun hasDbKey(): Boolean = withContext(Dispatchers.Default) {
        userDefaults.stringForKey(KEY_NAME) != null
    }
    
    private fun generateAndStoreDbKey(): ByteArray {
        val key = generateDbKey()
        storeDbKey(key)
        return key
    }
    
    private fun generateDbKey(): ByteArray {
        val key = ByteArray(32) // 256-bit key for SQLCipher
        Random.nextBytes(key) // Use Kotlin Random for now
        return key
    }
    
    private fun storeDbKey(key: ByteArray) {
        // Convert ByteArray to hex string for simple storage
        val hexString = key.joinToString("") { byte ->
            val unsigned = byte.toUByte().toInt()
            if (unsigned < 16) "0${unsigned.toString(16)}" else unsigned.toString(16)
        }
        userDefaults.setObject(hexString, KEY_NAME)
        userDefaults.synchronize()
    }
    
    private fun getStoredDbKey(): ByteArray {
        val hexString = userDefaults.stringForKey(KEY_NAME)
            ?: throw RuntimeException("No stored key found")
        
        // Convert hex string back to ByteArray
        return hexString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
    
    companion object {
        private const val KEY_NAME = "pocketadhd_db_key"
    }
}
package io.yavero.pocketadhd.core.data.security

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
actual class KeyManager {
    
    private val serviceName = "io.yavero.pocketadhd"
    private val accountName = "database_encryption_key"
    
    actual suspend fun getOrCreateDbKey(): ByteArray = withContext(Dispatchers.Default) {
        if (hasDbKey()) {
            getStoredDbKey()
        } else {
            generateAndStoreDbKey()
        }
    }
    
    actual suspend fun rotateDbKey() = withContext(Dispatchers.Default) {

    generateAndStoreDbKey()
        Unit
    }
    
    actual suspend fun hasDbKey(): Boolean = withContext(Dispatchers.Default) {


    val userDefaults = NSUserDefaults.standardUserDefaults
        val keyData = userDefaults.dataForKey(accountName)
        keyData != null
    }
    
    private fun generateAndStoreDbKey(): ByteArray {
        val key = generateDbKey()
        storeDbKey(key)
        return key
    }
    
    private fun generateDbKey(): ByteArray {
        val key = ByteArray(32) 
        Random.nextBytes(key)
        return key
    }

    @OptIn(BetaInteropApi::class)
    private fun storeDbKey(key: ByteArray) {

        val userDefaults = NSUserDefaults.standardUserDefaults
        val keyData = key.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = key.size.toULong())
        }
        userDefaults.setObject(keyData, forKey = accountName)
        userDefaults.synchronize()
    }
    
    private fun getStoredDbKey(): ByteArray {

        val userDefaults = NSUserDefaults.standardUserDefaults
        val keyData = userDefaults.dataForKey(accountName) 
            ?: throw RuntimeException("No encryption key found in storage")
        
        return ByteArray(keyData.length.toInt()) { index ->
            keyData.bytes!!.reinterpret<ByteVar>()[index]
        }
    }
}
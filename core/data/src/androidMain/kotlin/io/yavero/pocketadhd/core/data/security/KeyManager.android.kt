package io.yavero.pocketadhd.core.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android implementation of KeyManager using Android Keystore
 * 
 * Stores database encryption keys securely using:
 * - Android Keystore for master key generation and storage
 * - AES/GCM encryption for database key protection
 * - SharedPreferences for encrypted key blob storage
 */
actual class KeyManager(private val context: Context) {
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }
    
    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    actual suspend fun getOrCreateDbKey(): ByteArray = withContext(Dispatchers.IO) {
        if (hasDbKey()) {
            decryptDbKey()
        } else {
            generateAndStoreDbKey()
        }
    }
    
    actual suspend fun rotateDbKey() = withContext(Dispatchers.IO) {
        // Generate new key
        val newKey = generateDbKey()
        
        // Encrypt and store new key
        val encryptedData = encryptDbKey(newKey)
        storeEncryptedKey(encryptedData)
    }
    
    actual suspend fun hasDbKey(): Boolean = withContext(Dispatchers.IO) {
        sharedPrefs.contains(ENCRYPTED_KEY_PREF) && keyStore.containsAlias(MASTER_KEY_ALIAS)
    }
    
    private fun generateAndStoreDbKey(): ByteArray {
        // Generate master key in Keystore if it doesn't exist
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            generateMasterKey()
        }
        
        // Generate database key
        val dbKey = generateDbKey()
        
        // Encrypt and store database key
        val encryptedData = encryptDbKey(dbKey)
        storeEncryptedKey(encryptedData)
        
        return dbKey
    }
    
    private fun generateMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    private fun generateDbKey(): ByteArray {
        val key = ByteArray(32) // 256-bit key for SQLCipher
        SecureRandom().nextBytes(key)
        return key
    }
    
    private fun encryptDbKey(dbKey: ByteArray): EncryptedData {
        val secretKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val encryptedKey = cipher.doFinal(dbKey)
        val iv = cipher.iv
        
        return EncryptedData(encryptedKey, iv)
    }
    
    private fun decryptDbKey(): ByteArray {
        val encryptedData = getStoredEncryptedKey()
        val secretKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(128, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        
        return cipher.doFinal(encryptedData.encryptedKey)
    }
    
    private fun storeEncryptedKey(encryptedData: EncryptedData) {
        sharedPrefs.edit()
            .putString(ENCRYPTED_KEY_PREF, Base64.encodeToString(encryptedData.encryptedKey, Base64.DEFAULT))
            .putString(IV_PREF, Base64.encodeToString(encryptedData.iv, Base64.DEFAULT))
            .apply()
    }
    
    private fun getStoredEncryptedKey(): EncryptedData {
        val encryptedKeyString = sharedPrefs.getString(ENCRYPTED_KEY_PREF, null)
            ?: throw IllegalStateException("No encrypted key found")
        val ivString = sharedPrefs.getString(IV_PREF, null)
            ?: throw IllegalStateException("No IV found")
        
        return EncryptedData(
            encryptedKey = Base64.decode(encryptedKeyString, Base64.DEFAULT),
            iv = Base64.decode(ivString, Base64.DEFAULT)
        )
    }
    
    private data class EncryptedData(
        val encryptedKey: ByteArray,
        val iv: ByteArray
    )
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "pocketadhd_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFS_NAME = "pocketadhd_secure_prefs"
        private const val ENCRYPTED_KEY_PREF = "encrypted_db_key"
        private const val IV_PREF = "db_key_iv"
    }
}
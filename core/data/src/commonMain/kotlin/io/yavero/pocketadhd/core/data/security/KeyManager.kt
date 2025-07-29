package io.yavero.pocketadhd.core.data.security

/**
 * Cross-platform key management for database encryption
 * 
 * Provides secure key generation, storage, and rotation for SQLCipher database encryption.
 * Keys are stored in platform-specific secure storage:
 * - Android: Android Keystore with AES/GCM encryption
 * - iOS: iOS Keychain with kSecAttrAccessibleWhenUnlockedThisDeviceOnly
 */
expect class KeyManager {
    /**
     * Gets existing database key or creates a new one if none exists
     * @return 32-byte encryption key for SQLCipher
     */
    suspend fun getOrCreateDbKey(): ByteArray
    
    /**
     * Rotates the database encryption key
     * This will require database re-encryption
     */
    suspend fun rotateDbKey()
    
    /**
     * Checks if a database key exists in secure storage
     * @return true if key exists, false otherwise
     */
    suspend fun hasDbKey(): Boolean
}
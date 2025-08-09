package io.yavero.pocketadhd.core.data.security

expect class KeyManager {
    suspend fun getOrCreateDbKey(): ByteArray
    
    suspend fun rotateDbKey()
    
    suspend fun hasDbKey(): Boolean
}
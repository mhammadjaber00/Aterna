package io.yavero.pocketadhd.data.security

expect class KeyManager {
    suspend fun getOrCreateDbKey(): ByteArray
    
    suspend fun rotateDbKey()
    
    suspend fun hasDbKey(): Boolean
}
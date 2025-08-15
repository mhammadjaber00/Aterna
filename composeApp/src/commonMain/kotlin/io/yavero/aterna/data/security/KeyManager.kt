package io.yavero.aterna.data.security

expect class KeyManager {
    suspend fun getOrCreateDbKey(): ByteArray
    
    suspend fun rotateDbKey()
    
    suspend fun hasDbKey(): Boolean
}
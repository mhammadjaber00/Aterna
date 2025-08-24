package io.yavero.aterna.domain.repository

interface InventoryRepository {
    suspend fun getOwnedItemIds(heroId: String): Set<String>
    suspend fun hasItem(heroId: String, itemId: String): Boolean
    suspend fun addItemOnce(heroId: String, itemId: String)
}
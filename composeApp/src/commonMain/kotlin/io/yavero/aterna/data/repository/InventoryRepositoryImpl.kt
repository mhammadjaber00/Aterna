package io.yavero.aterna.data.repository

import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.domain.repository.InventoryRepository
import kotlin.time.ExperimentalTime

class InventoryRepositoryImpl(db: AternaDatabase) : InventoryRepository {
    private val query = db.heroItemQueries
    override suspend fun getOwnedItemIds(heroId: String) =
        query.selectItemsByHero(heroId).executeAsList().map { it.itemId }.toSet()

    override suspend fun hasItem(heroId: String, itemId: String): Boolean =
        query.hasHeroItem(heroId, itemId).executeAsOne()

    @OptIn(ExperimentalTime::class)
    override suspend fun addItemOnce(heroId: String, itemId: String) {
        val nowSeconds = kotlin.time.Clock.System.now().toEpochMilliseconds() / 1000
        query.insertHeroItem(heroId, itemId, nowSeconds)
    }
}

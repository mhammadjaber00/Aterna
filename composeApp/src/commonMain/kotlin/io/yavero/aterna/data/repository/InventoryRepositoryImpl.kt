package io.yavero.aterna.data.repository

import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.domain.repository.InventoryRepository
import kotlin.time.ExperimentalTime

class InventoryRepositoryImpl(db: AternaDatabase) : InventoryRepository {
    private val query = db.heroItemQueries
    override suspend fun getOwnedItemIds(heroId: String) =
        query.selectItemsByHero(heroId).executeAsList().map { it.itemId }.toSet()

    override suspend fun hasItem(heroId: String, itemId: String) =
        query.hasHeroItem(heroId, itemId).executeAsOne()

    @OptIn(ExperimentalTime::class)
    override suspend fun addItemOnce(heroId: String, itemId: String) {
        query.insertHeroItem(heroId, itemId, kotlin.time.Clock.System.now().epochSeconds)
    }
}

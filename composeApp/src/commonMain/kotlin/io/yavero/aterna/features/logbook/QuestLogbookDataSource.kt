package io.yavero.aterna.features.logbook

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Minimal data source implementation backed by QuestRepository and current hero.
 * It keyset-paginates using "beforeAt" seconds cursor.
 */
@OptIn(ExperimentalTime::class)
class QuestLogbookDataSource(
    private val questRepository: QuestRepository,
    private val heroRepository: HeroRepository,
) : LogbookDataSource {

    override suspend fun page(
        cursor: String?,
        pageSize: Int,
        query: String,
        types: Set<EventType>,
        includeIncomplete: Boolean,
        fromInstant: Instant?
    ): LogPage {
        val heroId = heroRepository.getCurrentHero()?.id
            ?: return LogPage(events = emptyList(), nextCursor = null, reachedEnd = true)

        val search = query.ifBlank { null }
        val typeNames: List<String> = if (types.isEmpty()) emptyList() else types.map { it.name }
        val fromSec: Long = fromInstant?.epochSeconds ?: 0L
        val toSec: Long = Long.MAX_VALUE
        val beforeAt: Long? = cursor?.toLongOrNull()

        val events = questRepository.logbookFetchPage(
            heroId = heroId,
            includeIncomplete = includeIncomplete,
            types = typeNames,
            fromEpochSec = fromSec,
            toEpochSec = toSec,
            search = search,
            beforeAt = beforeAt,
            limit = pageSize
        )

        val next = events.lastOrNull()?.at?.epochSeconds?.toString()
        val reachedEnd = events.size < pageSize
        return LogPage(events = events, nextCursor = next, reachedEnd = reachedEnd)
    }
}

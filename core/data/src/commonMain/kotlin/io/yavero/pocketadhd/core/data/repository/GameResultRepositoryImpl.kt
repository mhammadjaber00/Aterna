package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.GameResult
import io.yavero.pocketadhd.core.domain.repository.GameResultRepository
import io.yavero.pocketadhd.core.domain.repository.GameStats
import io.yavero.pocketadhd.core.domain.repository.GameTypeStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class GameResultRepositoryImpl(
    private val database: PocketAdhdDatabase
) : GameResultRepository {

    private val gameResultQueries = database.gameResultQueries

    override fun getAllGameResults(): Flow<List<GameResult>> {
        return gameResultQueries.selectAllGameResults()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getGameResultById(id: String): Flow<GameResult?> {
        return gameResultQueries.selectGameResultById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapEntityToDomain(it) }
            }
    }

    override fun getResultsByGameType(gameType: String): Flow<List<GameResult>> {
        return gameResultQueries.selectResultsByGameType(gameType)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getResultsByDateRange(startDate: Instant, endDate: Instant): Flow<List<GameResult>> {
        return gameResultQueries.selectResultsByDateRange(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getRecentResults(limit: Int): Flow<List<GameResult>> {
        return gameResultQueries.selectRecentResults(limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getRecentResultsByGameType(gameType: String, limit: Int): Flow<List<GameResult>> {
        return gameResultQueries.selectRecentResultsByGameType(gameType, limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getScoreTrendByGameType(gameType: String, since: Instant): Flow<List<GameResult>> {
        return gameResultQueries.selectScoreTrendByGameType(gameType, since.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    GameResult(
                        id = "", 
                        gameType = gameType,
                        timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
                        score = entity.score.toInt(),
                        durationSeconds = entity.durationSeconds.toInt()
                    )
                }
            }
    }

    override suspend fun getBestScoreByGameType(gameType: String): Int? {
        return gameResultQueries.selectBestScoreByGameType(gameType)
            .executeAsOneOrNull()
            ?.bestScore
            ?.toInt()
    }

    override suspend fun getGameTypeStats(gameType: String): GameTypeStats? {
        val result = gameResultQueries.selectAverageScoreByGameType(gameType)
            .executeAsOneOrNull()

        return result?.let {
            GameTypeStats(
                avgScore = it.avgScore ?: 0.0,
                avgDuration = it.avgDuration ?: 0.0,
                gameCount = it.gameCount.toInt()
            )
        }
    }

    override suspend fun getAllGameTypeStats(): List<GameStats> {
        return gameResultQueries.selectGameTypeStats()
            .executeAsList()
            .map { entity ->
                GameStats(
                    gameType = entity.gameType,
                    totalGames = entity.totalGames.toInt(),
                    avgScore = entity.avgScore ?: 0.0,
                    bestScore = entity.bestScore?.toInt() ?: 0,
                    avgDuration = entity.avgDuration ?: 0.0
                )
            }
    }

    override suspend fun insertGameResult(result: GameResult) {
        gameResultQueries.insertGameResult(
            id = result.id,
            gameType = result.gameType,
            timestamp = result.timestamp.toEpochMilliseconds(),
            score = result.score.toLong(),
            durationSeconds = result.durationSeconds.toLong()
        )
    }

    override suspend fun deleteGameResult(id: String) {
        gameResultQueries.deleteGameResult(id)
    }

    override suspend fun deleteResultsByGameType(gameType: String) {
        gameResultQueries.deleteResultsByGameType(gameType)
    }

    private fun mapEntityToDomain(entity: io.yavero.pocketadhd.core.data.database.GameResultEntity): GameResult {
        return GameResult(
            id = entity.id,
            gameType = entity.gameType,
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
            score = entity.score.toInt(),
            durationSeconds = entity.durationSeconds.toInt()
        )
    }
}
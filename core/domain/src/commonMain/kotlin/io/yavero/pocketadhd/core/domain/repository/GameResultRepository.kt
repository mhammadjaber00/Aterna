package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.GameResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

data class GameStats(
    val gameType: String,
    val totalGames: Int,
    val avgScore: Double,
    val bestScore: Int,
    val avgDuration: Double
)

data class GameTypeStats(
    val avgScore: Double,
    val avgDuration: Double,
    val gameCount: Int
)

interface GameResultRepository {
    fun getAllGameResults(): Flow<List<GameResult>>
    fun getGameResultById(id: String): Flow<GameResult?>
    fun getResultsByGameType(gameType: String): Flow<List<GameResult>>
    fun getResultsByDateRange(startDate: Instant, endDate: Instant): Flow<List<GameResult>>
    fun getRecentResults(limit: Int): Flow<List<GameResult>>
    fun getRecentResultsByGameType(gameType: String, limit: Int): Flow<List<GameResult>>
    fun getScoreTrendByGameType(gameType: String, since: Instant): Flow<List<GameResult>>
    suspend fun getBestScoreByGameType(gameType: String): Int?
    suspend fun getGameTypeStats(gameType: String): GameTypeStats?
    suspend fun getAllGameTypeStats(): List<GameStats>
    suspend fun insertGameResult(result: GameResult)
    suspend fun deleteGameResult(id: String)
    suspend fun deleteResultsByGameType(gameType: String)
}
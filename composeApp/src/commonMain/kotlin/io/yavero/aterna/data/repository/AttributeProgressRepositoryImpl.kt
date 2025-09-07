package io.yavero.aterna.data.repository

import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.domain.model.AttributeProgress
import io.yavero.aterna.domain.repository.AttributeProgressRepository

class AttributeProgressRepositoryImpl(
    private val db: AternaDatabase
) : AttributeProgressRepository {

    private val q = db.heroAttributeProgressQueries

    override suspend fun initIfMissing(heroId: String) {
        q.attrprog_initIfMissing(heroId)
    }

    override suspend fun get(heroId: String): AttributeProgress? {
        return q.attrprog_selectByHero(heroId).executeAsOneOrNull()?.let { r ->
            AttributeProgress(
                heroId = heroId,
                strXp = r.strXp.toInt(),
                perXp = r.perXp.toInt(),
                endXp = r.endXp.toInt(),
                chaXp = r.chaXp.toInt(),
                intXp = r.intXp.toInt(),
                agiXp = r.agiXp.toInt(),
                luckXp = r.luckXp.toInt(),
                lastGainDay = r.lastGainDay,
                dailyTotalAp = r.dailyTotalAp.toInt(),
                dailyStrAp = r.dailyStrAp.toInt(),
                dailyPerAp = r.dailyPerAp.toInt(),
                dailyEndAp = r.dailyEndAp.toInt(),
                dailyChaAp = r.dailyChaAp.toInt(),
                dailyIntAp = r.dailyIntAp.toInt(),
                dailyAgiAp = r.dailyAgiAp.toInt(),
                dailyLuckAp = r.dailyLuckAp.toInt(),
                dailyTypeDeepWork = r.dailyTypeDeepWork.toInt(),
                dailyTypeLearning = r.dailyTypeLearning.toInt(),
                dailyTypeCreative = r.dailyTypeCreative.toInt(),
                dailyTypeTraining = r.dailyTypeTraining.toInt(),
                dailyTypeAdmin = r.dailyTypeAdmin.toInt(),
                dailyTypeBreak = r.dailyTypeBreak.toInt(),
                dailyTypeOther = r.dailyTypeOther.toInt()
            )
        }
    }

    override suspend fun resetDaily(heroId: String, todayEpochDay: Long) {
        q.attrprog_resetDaily(todayEpochDay, heroId)
    }

    override suspend fun incrementTypeCounter(heroId: String, questTypeName: String) {
        q.attrprog_incTypeCounter(questTypeName, heroId)
    }

    override suspend fun addApDeltas(
        heroId: String,
        dStr: Int, dPer: Int, dEnd: Int, dCha: Int, dInt: Int, dAgi: Int, dLuck: Int
    ) {
        q.attrprog_addApDeltas(
            dStr.toLong(), dPer.toLong(), dEnd.toLong(), dCha.toLong(), dInt.toLong(), dAgi.toLong(), dLuck.toLong(),
            heroId
        )
    }

    override suspend fun applyResidues(
        heroId: String,
        rStr: Int, rPer: Int, rEnd: Int, rCha: Int, rInt: Int, rAgi: Int, rLuck: Int
    ) {
        q.attrprog_applyResidues(
            rStr.toLong(), rPer.toLong(), rEnd.toLong(), rCha.toLong(),
            rInt.toLong(), rAgi.toLong(), rLuck.toLong(), heroId
        )
    }
}
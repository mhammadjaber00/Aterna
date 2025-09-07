package io.yavero.aterna.domain.service.attr

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.repository.AttributeProgressRepository
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.SpecialThresholds

class AttributeProgressServiceImpl(
    private val attrRepo: AttributeProgressRepository,
    private val questRepo: QuestRepository,
    private val heroRepo: HeroRepository
) : AttributeProgressService {
    override suspend fun applyForCompletedQuest(heroAfterEconomy: Hero, quest: Quest): AttributeApplyResult {
        val heroId = heroAfterEconomy.id
        attrRepo.initIfMissing(heroId)
        val todayEpochDay = questRepo.analyticsTodayLocalDay()
        val current = attrRepo.get(heroId)
        if (current == null || current.lastGainDay != todayEpochDay) attrRepo.resetDaily(heroId, todayEpochDay)
        attrRepo.incrementTypeCounter(heroId, quest.questType.name)
        val deltas = questRepo.rulesSelectAggregate(quest.questType.name, quest.durationMinutes)
        attrRepo.addApDeltas(
            heroId,
            deltas.str,
            deltas.per,
            deltas.end,
            deltas.cha,
            deltas.int,
            deltas.agi,
            deltas.luck
        )
        val after = attrRepo.get(heroId)
        val startStr = heroAfterEconomy.strength
        val startPer = heroAfterEconomy.perception
        val startEnd = heroAfterEconomy.endurance
        val startCha = heroAfterEconomy.charisma
        val startInt = heroAfterEconomy.intelligence
        val startAgi = heroAfterEconomy.agility
        val startLuck = heroAfterEconomy.luck
        var residueStr = after?.strXp ?: 0
        var residuePer = after?.perXp ?: 0
        var residueEnd = after?.endXp ?: 0
        var residueCha = after?.chaXp ?: 0
        var residueInt = after?.intXp ?: 0
        var residueAgi = after?.agiXp ?: 0
        var residueLuck = after?.luckXp ?: 0
        var newStr = startStr
        var newPer = startPer
        var newEnd = startEnd
        var newCha = startCha
        var newInt = startInt
        var newAgi = startAgi
        var newLuck = startLuck
        var upStr = 0
        var upPer = 0
        var upEnd = 0
        var upCha = 0
        var upInt = 0
        var upAgi = 0
        var upLuck = 0
        while (residueStr >= SpecialThresholds.thresholdFor(newStr)) {
            residueStr -= SpecialThresholds.thresholdFor(newStr); newStr += 1; upStr += 1
        }
        while (residuePer >= SpecialThresholds.thresholdFor(newPer)) {
            residuePer -= SpecialThresholds.thresholdFor(newPer); newPer += 1; upPer += 1
        }
        while (residueEnd >= SpecialThresholds.thresholdFor(newEnd)) {
            residueEnd -= SpecialThresholds.thresholdFor(newEnd); newEnd += 1; upEnd += 1
        }
        while (residueCha >= SpecialThresholds.thresholdFor(newCha)) {
            residueCha -= SpecialThresholds.thresholdFor(newCha); newCha += 1; upCha += 1
        }
        while (residueInt >= SpecialThresholds.thresholdFor(newInt)) {
            residueInt -= SpecialThresholds.thresholdFor(newInt); newInt += 1; upInt += 1
        }
        while (residueAgi >= SpecialThresholds.thresholdFor(newAgi)) {
            residueAgi -= SpecialThresholds.thresholdFor(newAgi); newAgi += 1; upAgi += 1
        }
        while (residueLuck >= SpecialThresholds.thresholdFor(newLuck)) {
            residueLuck -= SpecialThresholds.thresholdFor(newLuck); newLuck += 1; upLuck += 1
        }
        if (newStr != startStr || newPer != startPer || newEnd != startEnd || newCha != startCha || newInt != startInt || newAgi != startAgi || newLuck != startLuck) {
            heroRepo.updateHeroSpecial(heroId, newStr, newPer, newEnd, newCha, newInt, newAgi, newLuck)
        }
        attrRepo.applyResidues(
            heroId,
            residueStr,
            residuePer,
            residueEnd,
            residueCha,
            residueInt,
            residueAgi,
            residueLuck
        )
        return AttributeApplyResult(rankUps = RankUps(upStr, upPer, upEnd, upCha, upInt, upAgi, upLuck))
    }
}

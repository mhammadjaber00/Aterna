package io.yavero.aterna.domain.service.attr

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.repository.AttributeProgressRepository
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.SpecialThresholds
import kotlin.time.ExperimentalTime

class DefaultAttributeProgressService(
    private val heroRepo: HeroRepository,
    private val questRepo: QuestRepository,
    private val attrRepo: AttributeProgressRepository
) : AttributeProgressService {

    @OptIn(ExperimentalTime::class)
    override suspend fun applyForCompletedQuest(hero: Hero, quest: Quest): AttributeProgressService.Result {
        val heroId = hero.id
        attrRepo.initIfMissing(heroId)
        val today = questRepo.analyticsTodayLocalDay()
        val st0 = attrRepo.get(heroId)
        if (st0 == null || st0.lastGainDay != today) attrRepo.resetDaily(heroId, today)

        val g = ApRules.gains(quest.questType, quest.durationMinutes)
        attrRepo.addApDeltas(heroId, g.str, g.per, g.end, g.cha, g.intl, g.agi, g.luck)

        val st = attrRepo.get(heroId)!!

        fun rankUp(rank: Int, xp: Int): Pair<Int, Int> {
            var r = rank
            var x = xp
            while (x >= SpecialThresholds.thresholdFor(r)) {
                x -= SpecialThresholds.thresholdFor(r)
                r++
            }
            return r to x
        }

        val (strR, strRes) = rankUp(hero.strength, st.strXp)
        val (perR, perRes) = rankUp(hero.perception, st.perXp)
        val (endR, endRes) = rankUp(hero.endurance, st.endXp)
        val (chaR, chaRes) = rankUp(hero.charisma, st.chaXp)
        val (intR, intRes) = rankUp(hero.intelligence, st.intXp)
        val (agiR, agiRes) = rankUp(hero.agility, st.agiXp)
        val (luckR, luckRes) = rankUp(hero.luck, st.luckXp)

        val newHero = hero.copy(
            strength = strR, perception = perR, endurance = endR,
            charisma = chaR, intelligence = intR, agility = agiR, luck = luckR
        )
        heroRepo.updateHero(newHero)

        attrRepo.applyResidues(heroId, strRes, perRes, endRes, chaRes, intRes, agiRes, luckRes)

        val ups = AttributeProgressService.RankUps(
            str = strR - hero.strength,
            per = perR - hero.perception,
            end = endR - hero.endurance,
            cha = chaR - hero.charisma,
            int = intR - hero.intelligence,
            agi = agiR - hero.agility,
            luck = luckR - hero.luck
        )
        return AttributeProgressService.Result(ups)
    }
}
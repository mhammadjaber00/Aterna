package io.yavero.pocketadhd.domain.util

import io.yavero.pocketadhd.domain.model.*
import kotlin.random.Random

object LootRoller {
    fun rollLoot(
        questDurationMinutes: Int,
        heroLevel: Int,
        classType: ClassType,
        serverSeed: Long
    ): QuestLoot {
        val random = Random(serverSeed)


        val baseXP = questDurationMinutes * 10
        val baseGold = (questDurationMinutes / 5) * 5


        val finalXP = (baseXP * classType.xpMultiplier).toInt()
        val finalGold = (baseGold * classType.goldMultiplier).toInt()


        val levelMultiplier = 1.0 + (heroLevel * 0.1)
        val bonusXP = (finalXP * levelMultiplier).toInt()
        val bonusGold = (finalGold * levelMultiplier).toInt()


        val itemDropChance = minOf(0.8, questDurationMinutes * 0.02)
        val items = if (random.nextDouble() < itemDropChance) {
            listOf(rollRandomItem(random, heroLevel))
        } else {
            emptyList()
        }

        return QuestLoot(
            xp = bonusXP,
            gold = bonusGold,
            items = items
        )
    }

    private fun rollRandomItem(random: Random, heroLevel: Int): Item {
        val rarity = when {
            heroLevel >= 20 && random.nextDouble() < 0.1 -> ItemRarity.LEGENDARY
            heroLevel >= 10 && random.nextDouble() < 0.2 -> ItemRarity.EPIC
            heroLevel >= 5 && random.nextDouble() < 0.3 -> ItemRarity.RARE
            else -> ItemRarity.COMMON
        }

        return generateItemByRarity(rarity, random)
    }

    private fun generateItemByRarity(rarity: ItemRarity, random: Random): Item {
        val itemPool = ItemPool.getItemsByRarity(rarity)
        return itemPool[random.nextInt(itemPool.size)]
    }
}
package io.yavero.pocketadhd.core.domain.util

import io.yavero.pocketadhd.core.domain.model.ClassType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LootRollerTest {

    @Test
    fun `rollLoot should be deterministic with same seed`() {
        val questDuration = 25
        val heroLevel = 5
        val classType = ClassType.WARRIOR
        val seed = 12345L


        val loot1 = LootRoller.rollLoot(questDuration, heroLevel, classType, seed)
        val loot2 = LootRoller.rollLoot(questDuration, heroLevel, classType, seed)


        assertEquals(loot1.xp, loot2.xp, "XP should be deterministic")
        assertEquals(loot1.gold, loot2.gold, "Gold should be deterministic")
        assertEquals(loot1.items.size, loot2.items.size, "Item count should be deterministic")

        if (loot1.items.isNotEmpty() && loot2.items.isNotEmpty()) {
            assertEquals(loot1.items[0].id, loot2.items[0].id, "Item drops should be deterministic")
        }
    }

    @Test
    fun `warrior should get XP bonus multiplier`() {
        val questDuration = 30
        val heroLevel = 1
        val seed = 54321L

        val warriorLoot = LootRoller.rollLoot(questDuration, heroLevel, ClassType.WARRIOR, seed)
        val mageLoot = LootRoller.rollLoot(questDuration, heroLevel, ClassType.MAGE, seed)






        assertTrue(warriorLoot.xp > mageLoot.xp, "Warrior should get more XP than Mage")
        assertEquals(396, warriorLoot.xp, "Warrior XP should be calculated correctly")
        assertEquals(330, mageLoot.xp, "Mage XP should be calculated correctly")
    }

    @Test
    fun `mage should get gold bonus multiplier`() {
        val questDuration = 25
        val heroLevel = 1
        val seed = 98765L

        val mageLoot = LootRoller.rollLoot(questDuration, heroLevel, ClassType.MAGE, seed)
        val warriorLoot = LootRoller.rollLoot(questDuration, heroLevel, ClassType.WARRIOR, seed)






        assertTrue(mageLoot.gold > warriorLoot.gold, "Mage should get more gold than Warrior")
        assertEquals(35, mageLoot.gold, "Mage gold should be calculated correctly")
        assertEquals(27, warriorLoot.gold, "Warrior gold should be calculated correctly")
    }

    @Test
    fun `long quest should provide better rewards`() {
        val shortQuest = 15
        val longQuest = 60
        val heroLevel = 5
        val classType = ClassType.WARRIOR
        val seed = 11111L

        val shortLoot = LootRoller.rollLoot(shortQuest, heroLevel, classType, seed)
        val longLoot = LootRoller.rollLoot(longQuest, heroLevel, classType, seed)


        assertTrue(longLoot.xp > shortLoot.xp, "Long quest should provide more XP")
        assertTrue(longLoot.gold > shortLoot.gold, "Long quest should provide more gold")


        val shortDropChance = kotlin.math.min(0.8, shortQuest * 0.02)
        val longDropChance = kotlin.math.min(0.8, longQuest * 0.02)

        assertTrue(longDropChance > shortDropChance, "Long quest should have higher item drop chance")
        assertEquals(0.3, shortDropChance, "Short quest drop chance should be 30%")
        assertEquals(0.8, longDropChance, "Long quest drop chance should be 80%")
    }

    @Test
    fun `level bonus should increase rewards`() {
        val questDuration = 20
        val lowLevel = 1
        val highLevel = 10
        val classType = ClassType.MAGE
        val seed = 22222L

        val lowLevelLoot = LootRoller.rollLoot(questDuration, lowLevel, classType, seed)
        val highLevelLoot = LootRoller.rollLoot(questDuration, highLevel, classType, seed)


        assertTrue(highLevelLoot.xp > lowLevelLoot.xp, "High level should provide more XP")
        assertTrue(highLevelLoot.gold > lowLevelLoot.gold, "High level should provide more gold")


        val expectedLowLevelMultiplier = 1.0 + (lowLevel * 0.1)
        val expectedHighLevelMultiplier = 1.0 + (highLevel * 0.1)

        assertEquals(1.1, expectedLowLevelMultiplier, "Low level multiplier should be 1.1")
        assertEquals(2.0, expectedHighLevelMultiplier, "High level multiplier should be 2.0")
    }
}
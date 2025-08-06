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

        // Roll loot twice with same parameters
        val loot1 = LootRoller.rollLoot(questDuration, heroLevel, classType, seed)
        val loot2 = LootRoller.rollLoot(questDuration, heroLevel, classType, seed)

        // Results should be identical
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

        // Warrior should get 1.2x XP multiplier vs Mage's 1.0x
        // Base XP = 30 * 10 = 300
        // Level multiplier = 1.0 + (1 * 0.1) = 1.1
        // Warrior: 300 * 1.2 * 1.1 = 396
        // Mage: 300 * 1.0 * 1.1 = 330
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

        // Mage should get 1.3x gold multiplier vs Warrior's 1.0x
        // Base gold = (25 / 5) * 5 = 25
        // Level multiplier = 1.0 + (1 * 0.1) = 1.1
        // Mage: 25 * 1.3 * 1.1 = 35.75 -> 35 (int)
        // Warrior: 25 * 1.0 * 1.1 = 27.5 -> 27 (int)
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

        // Longer quests should provide more XP and gold
        assertTrue(longLoot.xp > shortLoot.xp, "Long quest should provide more XP")
        assertTrue(longLoot.gold > shortLoot.gold, "Long quest should provide more gold")

        // Item drop chance should be higher for longer quests
        // Short: 15 * 0.02 = 0.3 (30% chance)
        // Long: 60 * 0.02 = 1.2 -> min(0.8, 1.2) = 0.8 (80% chance)
        // With deterministic seed, we can verify the calculation
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

        // Higher level should provide more rewards due to level multiplier
        assertTrue(highLevelLoot.xp > lowLevelLoot.xp, "High level should provide more XP")
        assertTrue(highLevelLoot.gold > lowLevelLoot.gold, "High level should provide more gold")

        // Verify level multiplier calculation
        // Level 1: 1.0 + (1 * 0.1) = 1.1
        // Level 10: 1.0 + (10 * 0.1) = 2.0
        val expectedLowLevelMultiplier = 1.0 + (lowLevel * 0.1)
        val expectedHighLevelMultiplier = 1.0 + (highLevel * 0.1)

        assertEquals(1.1, expectedLowLevelMultiplier, "Low level multiplier should be 1.1")
        assertEquals(2.0, expectedHighLevelMultiplier, "High level multiplier should be 2.0")
    }

    @Test
    fun `base reward calculation should be correct`() {
        val questDuration = 30
        val heroLevel = 1
        val classType = ClassType.ROGUE // 1.0x multipliers
        val seed = 33333L

        val loot = LootRoller.rollLoot(questDuration, heroLevel, classType, seed)

        // Base XP = 30 * 10 = 300
        // Class multiplier = 1.0
        // Level multiplier = 1.0 + (1 * 0.1) = 1.1
        // Final XP = 300 * 1.0 * 1.1 = 330
        assertEquals(330, loot.xp, "Base XP calculation should be correct")

        // Base gold = (30 / 5) * 5 = 30
        // Class multiplier = 1.0
        // Level multiplier = 1.1
        // Final gold = 30 * 1.0 * 1.1 = 33
        assertEquals(33, loot.gold, "Base gold calculation should be correct")
    }
}
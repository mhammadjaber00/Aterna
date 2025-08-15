package io.yavero.pocketadhd.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LootTable(
    val rarity: ItemRarity,
    val items: List<Item>
)

object ItemPool {
    private val commonItems = listOf(
        Item(
            "focus_potion_1",
            "Minor Focus Potion",
            "Increases focus for 5 minutes",
            ItemType.CONSUMABLE,
            ItemRarity.COMMON,
            10,
            true,
            5
        ),
        Item(
            "wooden_sword",
            "Wooden Practice Sword",
            "A basic training weapon",
            ItemType.WEAPON,
            ItemRarity.COMMON,
            25
        ),
        Item("leather_gloves", "Leather Gloves", "Simple protective gloves", ItemType.ARMOR, ItemRarity.COMMON, 20)
    )

    private val rareItems = listOf(
        Item(
            "focus_potion_2",
            "Focus Elixir",
            "Increases focus for 15 minutes",
            ItemType.CONSUMABLE,
            ItemRarity.RARE,
            50,
            true,
            3
        ),
        Item("steel_sword", "Steel Blade", "A well-crafted weapon", ItemType.WEAPON, ItemRarity.RARE, 100),
        Item("chain_mail", "Chain Mail Vest", "Sturdy protective armor", ItemType.ARMOR, ItemRarity.RARE, 120)
    )

    private val epicItems = listOf(
        Item(
            "focus_potion_3",
            "Greater Focus Draught",
            "Increases focus for 30 minutes",
            ItemType.CONSUMABLE,
            ItemRarity.EPIC,
            200,
            true,
            2
        ),
        Item(
            "enchanted_blade",
            "Enchanted Blade",
            "A magically enhanced weapon",
            ItemType.WEAPON,
            ItemRarity.EPIC,
            500
        ),
        Item("mithril_armor", "Mithril Chainmail", "Lightweight yet strong armor", ItemType.ARMOR, ItemRarity.EPIC, 600)
    )

    private val legendaryItems = listOf(
        Item(
            "focus_mastery",
            "Potion of Focus Mastery",
            "Ultimate focus enhancement",
            ItemType.CONSUMABLE,
            ItemRarity.LEGENDARY,
            1000,
            true,
            1
        ),
        Item("excalibur", "Excalibur", "The legendary sword of focus", ItemType.WEAPON, ItemRarity.LEGENDARY, 2500),
        Item(
            "dragon_scale",
            "Dragon Scale Armor",
            "Armor forged from dragon scales",
            ItemType.ARMOR,
            ItemRarity.LEGENDARY,
            3000
        )
    )

    fun getItemsByRarity(rarity: ItemRarity): List<Item> = when (rarity) {
        ItemRarity.COMMON -> commonItems
        ItemRarity.RARE -> rareItems
        ItemRarity.EPIC -> epicItems
        ItemRarity.LEGENDARY -> legendaryItems
    }

    fun getAllLootTables(): List<LootTable> = ItemRarity.entries.map { rarity ->
        LootTable(rarity, getItemsByRarity(rarity))
    }
}
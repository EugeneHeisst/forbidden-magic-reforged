package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.Registration;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();
    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> SKULL_AXE;
    public static final DeferredItem<Item> EMERALD_FRAGMENT;
    public static final DeferredItem<Item> MORPH_PICKAXE;
    public static final DeferredItem<Item> MORPH_SWORD;
    public static final DeferredItem<Item> MORPH_SHOVEL;
    public static final DeferredItem<Item> MORPH_AXE;
    public static final DeferredItem<Item> MOB_CRYSTAL;
    public static final DeferredItem<Item> DIABOLIST_FORK;

    static {
        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            DEADLY_SHARDS.put(vice, ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.registerSimpleItem("gluttony_shard", properties -> Registration.food(properties, 2, 0.1F));
        SKULL_AXE = ITEMS.registerSimpleItem("skull_axe", properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
        MORPH_PICKAXE = ITEMS.registerItem("morph_pickaxe", MorphToolItem::new,
            properties -> repairable(Registration.pickaxe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 2.0F, -2.8F)));
        MORPH_SWORD = ITEMS.registerItem("morph_sword", MorphToolItem::new,
            properties -> repairable(Registration.sword(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 4.0F, -2.4F)));
        MORPH_SHOVEL = ITEMS.registerItem("morph_shovel", MorphToolItem::new,
            properties -> repairable(Registration.shovel(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F)));
        MORPH_AXE = ITEMS.registerItem("morph_axe", MorphToolItem::new,
            properties -> repairable(Registration.axe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -3.0F)));
        MOB_CRYSTAL = ITEMS.registerItem("mob_crystal", MobCrystalItem::new,
            properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        DIABOLIST_FORK = ITEMS.registerItem("diabolist_fork", Item::new,
            properties -> Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.THAUMIUM_TOOL, 3.0F, -2.4F)
                .repairable(net.minecraft.world.item.Items.QUARTZ));
    }

    private FmItems() {
    }

    private static Item.Properties repairable(Item.Properties properties) {
        return properties.repairable(DEADLY_SHARDS.get("envy").get());
    }

    public static Identifier id(String vice) {
        return Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, vice + "_shard");
    }
}

package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.platform.Registration;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();

    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> EMERALD_FRAGMENT;
    public static final DeferredItem<Item> TAINT_COAL;
    public static final DeferredItem<Item> BLACK_DYE;
    public static final DeferredItem<Item> TAINT_FRUIT;
    public static final DeferredItem<Item> TAINTED_ROD;
    public static final DeferredItem<Item> INFERNAL_ROD;
    public static final DeferredItem<Item> WITCHWOOD_ROD;
    public static final DeferredItem<Item> PROFANE_ROD;
    public static final DeferredItem<Item> WITCHWOOD_STAFF_ROD;
    public static final DeferredItem<MobCrystalItem> MOB_CRYSTAL;
    public static final DeferredItem<DiabolistForkItem> DIABOLIST_FORK;

    static {
        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            if (vice.equals("lust")) {
                DEADLY_SHARDS.put(vice, ITEMS.registerItem("lust_shard", LustShardItem::new));
            } else {
                DEADLY_SHARDS.put(vice, ITEMS.registerSimpleItem(vice + "_shard"));
            }
        }

        GLUTTONY_SHARD = ITEMS.register(
            "gluttony_shard",
            () -> new Item(Registration.food(new Item.Properties(), 2, 0.1F))
        );
        EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
        TAINT_COAL = ITEMS.registerSimpleItem("taint_coal");
        BLACK_DYE = ITEMS.registerItem("black_dye", BlackDyeItem::new);
        TAINT_FRUIT = ITEMS.registerItem("taint_fruit", TaintFruitItem::new);
        TAINTED_ROD = ITEMS.registerSimpleItem("tainted_rod");
        INFERNAL_ROD = ITEMS.registerSimpleItem("infernal_rod");
        WITCHWOOD_ROD = ITEMS.registerSimpleItem("witchwood_rod");
        PROFANE_ROD = ITEMS.registerSimpleItem("profane_rod");
        WITCHWOOD_STAFF_ROD = ITEMS.registerSimpleItem("witchwood_staff_rod");
        MOB_CRYSTAL = ITEMS.registerItem("mob_crystal", MobCrystalItem::new);
        DIABOLIST_FORK = ITEMS.registerItem("diabolist_fork", DiabolistForkItem::new);
    }

    private FmItems() {
    }

    public static ResourceLocation id(String vice) {
        return ResourceLocation.fromNamespaceAndPath(ForbiddenMagic.MODID, vice + "_shard");
    }
}

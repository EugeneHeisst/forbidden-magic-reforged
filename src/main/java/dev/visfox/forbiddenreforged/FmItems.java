package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.platform.Registration;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();
    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> SKULL_AXE;

    static {
        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            DEADLY_SHARDS.put(vice, ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.registerSimpleItem("gluttony_shard", properties -> Registration.food(properties, 2, 0.1F));
        SKULL_AXE = ITEMS.registerSimpleItem("skull_axe", properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    private FmItems() {
    }

    public static Identifier id(String vice) {
        return Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, vice + "_shard");
    }
}

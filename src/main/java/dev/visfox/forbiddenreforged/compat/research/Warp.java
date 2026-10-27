package dev.visfox.forbiddenreforged.compat.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class Warp {
    public static final java.util.Map<String, Integer> RESEARCH = new java.util.HashMap<>(),
            ITEMS = new java.util.HashMap<>();

    public static void registerItem(ResourceLocation item, int amount) {
        ITEMS.put(item.toString(), amount);
    }

    public static void add(Player player, int amount, WarpType type) {
        dev.eugheist.tc4modern.PlayerWarp.add(
                player, dev.eugheist.tc4modern.WarpState.Kind.valueOf(type.name()), amount);
    }
}

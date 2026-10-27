package dev.visfox.forbiddenreforged.compat.wand;

import dev.eugheist.tc4modern.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public final class WandRegistry {
    public static final WandCap CAP_IRON = new WandCap("iron"),
            CAP_GOLD = new WandCap("gold"),
            CAP_THAUMIUM = new WandCap("thaumium"),
            CAP_VOID = new WandCap("void");
    public static final Map<String, RodUpdate> UPDATES = new HashMap<>();
    public static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();

    public static WandRod findRod(String tag) {
        return dev.eugheist.tc4modern.WandParts.RODDEFS.containsKey(tag) ? new WandRod(tag) : null;
    }

    public static WandCap findCap(String tag) {
        return dev.eugheist.tc4modern.WandParts.CAPS.containsKey(tag) ? new WandCap(tag) : null;
    }

    public static void registerRod(
            String tag,
            int capacity,
            ResourceLocation item,
            int cost,
            RodUpdate update,
            boolean glow,
            boolean staff,
            boolean runes,
            ResourceLocation texture) {
        String key = tag + (staff && !tag.endsWith("_staff") ? "_staff" : "");
        var def =
                new dev.eugheist.tc4modern.WandParts.Rod(
                        key,
                        capacity,
                        () ->
                                dev.visfox.forbiddenreforged.compat.crafting.TcIngredient.item(
                                                item.toString())
                                        .item(),
                        cost,
                        null,
                        glow);
        if (dev.eugheist.tc4modern.WandParts.RODDEFS.putIfAbsent(key, def) != null)
            throw new IllegalStateException("Duplicate rod " + key);
        if (update != null) UPDATES.put(key, update);
        TEXTURES.put("rod:" + key, texture);
    }

    public static void registerCap(
            String tag,
            float modifier,
            List<Aspect> aspects,
            float special,
            ResourceLocation item,
            int cost,
            Object update,
            ResourceLocation texture) {
        var cap =
                new dev.eugheist.tc4modern.WandParts.Cap(
                        tag,
                        modifier,
                        aspects,
                        special,
                        () ->
                                dev.visfox.forbiddenreforged.compat.crafting.TcIngredient.item(
                                                item.toString())
                                        .item(),
                        cost);
        if (dev.eugheist.tc4modern.WandParts.CAPS.putIfAbsent(tag, cap) != null)
            throw new IllegalStateException("Duplicate cap " + tag);
        TEXTURES.put("cap:" + tag, texture);
    }

    public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e) {
        var p = e.getEntity();
        if (p.level().isClientSide) return;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var s = p.getInventory().getItem(i);
            if (!Wands.isWand(s)) continue;
            var u = UPDATES.get(Wands.getRod(s).tag());
            if (u != null) u.onUpdate(s, p);
        }
    }
}

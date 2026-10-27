package dev.visfox.forbiddenreforged.compat.research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import thaumcraft.api.aspects.Aspect;

public final class ResearchManager {
    public static boolean isComplete(Player p, String key) {
        return dev.eugheist.tc4modern.ResearchProgress.completed(p.getPersistentData())
                .contains(key);
    }

    public static void addAspectPool(ServerPlayer p, Aspect a, int count) {
        var k = dev.eugheist.tc4modern.ResearchKnowledge.load(p.getPersistentData());
        k.addAspectPool("current", a, (short) count);
        dev.eugheist.tc4modern.ResearchKnowledge.save(p.getPersistentData(), k);
        dev.eugheist.tc4modern.ThaumometerItem.sync(p, "aspect_pool");
    }
}

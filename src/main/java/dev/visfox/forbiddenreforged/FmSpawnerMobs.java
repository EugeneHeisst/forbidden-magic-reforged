package dev.visfox.forbiddenreforged;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import thaumcraft.api.aspects.Aspect;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public final class FmSpawnerMobs {
    private static final Map<String, Aspect> MOBS =
            Map.ofEntries(
                    Map.entry("minecraft:zombie", Aspect.FLESH),
                    Map.entry("minecraft:skeleton", Aspect.DEATH),
                    Map.entry("minecraft:creeper", Aspect.FIRE),
                    Map.entry("minecraft:horse", Aspect.BEAST),
                    Map.entry("minecraft:pig", Aspect.BEAST),
                    Map.entry("minecraft:sheep", Aspect.CLOTH),
                    Map.entry("minecraft:cow", Aspect.BEAST),
                    Map.entry("minecraft:mooshroom", Aspect.PLANT),
                    Map.entry("minecraft:ocelot", Aspect.BEAST),
                    Map.entry("minecraft:chicken", Aspect.FLIGHT),
                    Map.entry("minecraft:squid", Aspect.SENSES),
                    Map.entry("minecraft:wolf", Aspect.BEAST),
                    Map.entry("minecraft:bat", Aspect.FLIGHT),
                    Map.entry("minecraft:spider", Aspect.CLOTH),
                    Map.entry("minecraft:slime", Aspect.SLIME),
                    Map.entry("minecraft:ghast", Aspect.FIRE),
                    Map.entry("minecraft:zombified_piglin", Aspect.GREED),
                    Map.entry("minecraft:enderman", Aspect.ELDRITCH),
                    Map.entry("minecraft:cave_spider", Aspect.POISON),
                    Map.entry("minecraft:silverfish", Aspect.GREED),
                    Map.entry("minecraft:blaze", Aspect.FIRE),
                    Map.entry("minecraft:magma_cube", Aspect.FIRE),
                    Map.entry("minecraft:witch", Aspect.MAGIC),
                    Map.entry("minecraft:villager", Aspect.GREED),
                    Map.entry("tc4modern:firebat", Aspect.FIRE),
                    Map.entry("tc4modern:wisp", Aspect.AURA),
                    Map.entry("tc4modern:thaumic_slime", Aspect.TAINT),
                    Map.entry("tc4modern:brainy_zombie", Aspect.MIND),
                    Map.entry("tc4modern:giant_brainy_zombie", Aspect.MIND),
                    Map.entry("tc4modern:taint_spider", Aspect.TAINT),
                    Map.entry("tc4modern:taint_swarm", Aspect.TAINT),
                    Map.entry("tc4modern:tainted_pig", Aspect.TAINT),
                    Map.entry("tc4modern:tainted_sheep", Aspect.TAINT),
                    Map.entry("tc4modern:tainted_cow", Aspect.TAINT),
                    Map.entry("tc4modern:tainted_chicken", Aspect.TAINT),
                    Map.entry("tc4modern:tainted_villager", Aspect.TAINT),
                    Map.entry("tc4modern:cultist_knight", Aspect.ELDRITCH),
                    Map.entry("tc4modern:cultist_cleric", Aspect.ELDRITCH),
                    Map.entry("tc4modern:eldritch_crab", Aspect.ELDRITCH),
                    Map.entry("tc4modern:inhabited_zombie", Aspect.ELDRITCH),
                    Map.entry("tc4modern:pech", Aspect.GREED),
                    Map.entry("tc4modern:eldritch_guardian", Aspect.ELDRITCH));

    private static final Set<String> BOSSES =
            Set.of(
                    "minecraft:ender_dragon",
                    "minecraft:wither",
                    "tc4modern:cultist_leader",
                    "tc4modern:eldritch_golem",
                    "tc4modern:eldritch_warden");

    private FmSpawnerMobs() {}

    public static @Nullable Aspect aspect(@Nullable String id) {
        if (id == null) return null;
        if (id.equals("minecraft:ghast")) return FmAspects.NETHER;
        return MOBS.get(id);
    }

    public static List<String> ids() {
        return List.copyOf(MOBS.keySet());
    }

    public static boolean canImprint(EntityType<?> type) {
        if (!FmConfig.wrathCage()) {
            return false;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        return MOBS.containsKey(id) || (FmConfig.wrathCrazy() && !isBoss(type));
    }

    public static boolean isBoss(EntityType<?> type) {
        return BOSSES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
    }
}

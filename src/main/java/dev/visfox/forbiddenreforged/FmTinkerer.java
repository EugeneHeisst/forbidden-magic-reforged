package dev.visfox.forbiddenreforged;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.lang.reflect.Constructor;
import java.util.Map;

public final class FmTinkerer {
    private static int registered;
    public static int totalEntries = 40;

    private FmTinkerer() {}

    public static void register() {
        if (!FmConfig.enchantmentsEnabled() || !ModList.get().isLoaded("ttinkerer")) {
            return;
        }
        try {
            Class.forName("dev.eugheist.ttmodern.EnchanterCosts");
        } catch (ClassNotFoundException error) {
            throw new IllegalStateException("Unsupported Thaumic Tinkerer port", error);
        }
    }

    public static Map<String, Object> addCosts(Map<String, Object> original) {
        if (!FmConfig.enchantmentsEnabled()) return original;
        try {
            var type = Class.forName("dev.eugheist.ttmodern.EnchanterCosts$Entry");
            var constructor =
                    type.getDeclaredConstructor(
                            String.class, boolean.class, String.class, Map.class);
            constructor.setAccessible(true);
            Map<String, Object> costs = new java.util.LinkedHashMap<>(original);
            add(
                    costs,
                    constructor,
                    FmEnchantments.WRATH,
                    "textures/misc/wrath.png",
                    new AspectList().add(Aspect.ENTROPY, 6).add(Aspect.FIRE, 6),
                    "WRATH");
            if (FmConfig.greedyEnchantmentEnabled())
                add(
                        costs,
                        constructor,
                        FmEnchantments.GREEDY,
                        "textures/misc/greedy.png",
                        new AspectList()
                                .add(Aspect.ENTROPY, 50)
                                .add(Aspect.EARTH, 30)
                                .add(Aspect.ORDER, 20),
                        "GREEDY");
            add(
                    costs,
                    constructor,
                    FmEnchantments.EDUCATIONAL,
                    "textures/misc/educational.png",
                    new AspectList()
                            .add(Aspect.AIR, 10)
                            .add(Aspect.FIRE, 10)
                            .add(Aspect.WATER, 10)
                            .add(Aspect.EARTH, 10)
                            .add(Aspect.ORDER, 15)
                            .add(Aspect.ENTROPY, 15),
                    "EDUCATIONAL");
            add(
                    costs,
                    constructor,
                    FmEnchantments.CONSUMING,
                    "textures/misc/consuming.png",
                    new AspectList().add(Aspect.ENTROPY, 10),
                    "CONSUMING");
            add(
                    costs,
                    constructor,
                    FmEnchantments.CORRUPTING,
                    "textures/misc/corrupting.png",
                    new AspectList()
                            .add(Aspect.ENTROPY, 20)
                            .add(Aspect.FIRE, 15)
                            .add(Aspect.EARTH, 10),
                    "CORRUPTING");
            add(
                    costs,
                    constructor,
                    FmEnchantments.CLUSTER,
                    "textures/misc/lucrative.png",
                    new AspectList().add(Aspect.FIRE, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5),
                    "CLUSTER");
            add(
                    costs,
                    constructor,
                    FmEnchantments.IMPACT,
                    "textures/misc/impact.png",
                    new AspectList().add(Aspect.ENTROPY, 30).add(Aspect.EARTH, 30),
                    "IMPACT");
            add(
                    costs,
                    constructor,
                    FmEnchantments.VOIDTOUCHED,
                    "textures/misc/voidtouched.png",
                    new AspectList().add(Aspect.ENTROPY, 70),
                    "VOIDTOUCHED");
            registered = costs.size() - original.size();
            totalEntries = costs.size();
            return java.util.Collections.unmodifiableMap(costs);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                    "Thaumic Tinkerer enchantment integration failed", error);
        }
    }

    public static int registeredCount() {
        return registered;
    }

    private static void add(
            Map<String, Object> costs,
            Constructor<?> constructor,
            ResourceKey<Enchantment> key,
            String texture,
            AspectList aspects,
            String research)
            throws ReflectiveOperationException {
        Map<Aspect, Integer> base = new java.util.LinkedHashMap<>();
        for (var aspect : aspects.getAspects()) base.put(aspect, aspects.getAmount(aspect));
        costs.put(
                key.location().toString(),
                constructor.newInstance(
                        ForbiddenMagic.id(texture).toString(),
                        false,
                        research,
                        java.util.Collections.unmodifiableMap(base)));
    }
}

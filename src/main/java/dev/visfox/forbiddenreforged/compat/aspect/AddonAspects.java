package dev.visfox.forbiddenreforged.compat.aspect;

import net.minecraft.resources.ResourceLocation;

import thaumcraft.api.aspects.Aspect;

import java.util.*;
import java.util.function.Consumer;

public final class AddonAspects {
    private static final List<Consumer<AddonAspects>> REGISTRARS = new ArrayList<>();
    private static boolean initialized;

    public static void addRegistrar(Consumer<AddonAspects> registrar) {
        REGISTRARS.add(registrar);
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        for (var r : REGISTRARS) r.accept(new AddonAspects());
    }

    public Aspect register(
            String tag, int color, Aspect[] parts, int blend, ResourceLocation texture) {
        if (Aspect.getAspect(tag) != null)
            throw new IllegalStateException("Duplicate aspect " + tag);
        return new Aspect(tag, color, parts, texture, blend);
    }
}

package dev.visfox.forbiddenreforged.compat.aspect;

import net.minecraft.resources.ResourceLocation;

import thaumcraft.api.aspects.AspectList;

public final class AspectRegistry {
    public void register(String id, AspectList tags) {
        dev.eugheist.tc4modern.api.TC4Addons.registerObjectTag(ResourceLocation.parse(id), tags);
    }
}

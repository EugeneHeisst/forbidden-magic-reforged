package dev.visfox.forbiddenreforged.compat.research;

public final class ResearchCategories {
    public static void registerCategory(
            String key,
            net.minecraft.resources.ResourceLocation icon,
            net.minecraft.resources.ResourceLocation background) {
        thaumcraft.api.research.ResearchCategories.registerCategory(key, icon, background);
    }
}

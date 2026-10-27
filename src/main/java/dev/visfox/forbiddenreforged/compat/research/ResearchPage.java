package dev.visfox.forbiddenreforged.compat.research;

public final class ResearchPage {
    public static thaumcraft.api.research.ResearchPage text(String key) {
        return new thaumcraft.api.research.ResearchPage(key);
    }

    private static thaumcraft.api.research.ResearchPage recipe(
            String key, thaumcraft.api.research.ResearchPage.PageType type) {
        var p = text("");
        p.type = type;
        p.recipeKey = key;
        return p;
    }

    public static thaumcraft.api.research.ResearchPage infusion(String key) {
        return recipe(key, thaumcraft.api.research.ResearchPage.PageType.INFUSION_CRAFTING);
    }

    public static thaumcraft.api.research.ResearchPage infusionEnchantment(String key) {
        return recipe(
                "InfEnchFM" + key, thaumcraft.api.research.ResearchPage.PageType.INFUSION_CRAFTING);
    }

    public static thaumcraft.api.research.ResearchPage arcane(String key) {
        return recipe(key, thaumcraft.api.research.ResearchPage.PageType.ARCANE_CRAFTING);
    }

    public static thaumcraft.api.research.ResearchPage crucible(String key) {
        return recipe(key, thaumcraft.api.research.ResearchPage.PageType.CRUCIBLE_CRAFTING);
    }

    public static thaumcraft.api.research.ResearchPage crafting(String key) {
        return recipe(
                dev.visfox.forbiddenreforged.compat.crafting.TcRecipes.CRAFTING.getOrDefault(
                        key, key),
                thaumcraft.api.research.ResearchPage.PageType.NORMAL_CRAFTING);
    }
}

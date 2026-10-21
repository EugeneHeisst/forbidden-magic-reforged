package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Set;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;

public final class FmRecipes {
    public static final Set<String> OWN_KEYS = Set.of("SKULLAXE");

    private FmRecipes() {
    }

    public static void register() {
        TcRecipes.register(new InfusionRecipe(
            "SKULLAXE",
            "SKULLAXE",
            TcStack.of(FmItems.SKULL_AXE.getId().toString(), 1),
            1,
            new AspectList().add(FmAspects.WRATH, 8).add(Aspect.WEAPON, 8).add(FmAspects.NETHER, 8),
            TcIngredient.item("thaumcraft:thaumium_axe"),
            List.of(
                TcIngredient.item(FmItems.id("wrath").toString()),
                TcIngredient.item(FmItems.id("wrath").toString()),
                TcIngredient.item("minecraft:diamond"),
                TcIngredient.item("minecraft:wither_skeleton_skull")
            )
        ));
    }
}

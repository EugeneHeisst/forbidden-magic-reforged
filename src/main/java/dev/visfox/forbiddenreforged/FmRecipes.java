package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Set;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;

public final class FmRecipes {
    public static final Set<String> OWN_KEYS = Set.of("SKULLAXE", "WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING");

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
        if (FmConfig.enchantmentsEnabled()) {
            TcRecipes.register(new InfusionEnchantmentRecipe("WRATH", "WRATH", ForbiddenMagic.id("wrath"), 8,
                new AspectList().add(FmAspects.WRATH, 16).add(Aspect.WEAPON, 16).add(FmAspects.NETHER, 8),
                List.of(
                    TcIngredient.item("thaumcraft:salis_mundus"),
                    TcIngredient.item("minecraft:diamond_sword"),
                    TcIngredient.item(FmItems.id("wrath").toString()),
                    TcIngredient.item(FmItems.id("wrath").toString()),
                    TcIngredient.item(FmItems.id("wrath").toString())
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("GREEDY", "GREEDY", ForbiddenMagic.id("greedy"), 4,
                new AspectList().add(FmAspects.NETHER, 16).add(Aspect.WEAPON, 8).add(Aspect.GREED, 16),
                List.of(
                    TcIngredient.item("minecraft:golden_sword"),
                    TcIngredient.item("minecraft:diamond"),
                    TcIngredient.item(FmItems.id("greed").toString()),
                    TcIngredient.item(FmItems.id("greed").toString()),
                    TcIngredient.item("thaumcraft:salis_mundus")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("CONSUMING", "CONSUMING", ForbiddenMagic.id("consuming"), 3,
                new AspectList().add(Aspect.VOID, 8).add(Aspect.TOOL, 8).add(Aspect.HUNGER, 8),
                List.of(
                    TcIngredient.item("minecraft:iron_pickaxe"),
                    TcIngredient.item("minecraft:lava_bucket"),
                    TcIngredient.item("thaumcraft:salis_mundus")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("EDUCATIONAL", "EDUCATIONAL", ForbiddenMagic.id("educational"), 3,
                new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.MIND, 8),
                List.of(
                    TcIngredient.item("thaumcraft:zombie_brain"),
                    TcIngredient.item("minecraft:book"),
                    TcIngredient.item("thaumcraft:salis_mundus")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("CORRUPTING", "CORRUPTING", ForbiddenMagic.id("corrupting"), 4,
                new AspectList().add(FmAspects.NETHER, 16).add(Aspect.EXCHANGE, 16).add(Aspect.CRYSTAL, 8),
                List.of(
                    TcIngredient.item("minecraft:nether_wart"),
                    TcIngredient.item("minecraft:soul_sand"),
                    TcIngredient.item("thaumcraft:salis_mundus")
                )));
        }
    }
}

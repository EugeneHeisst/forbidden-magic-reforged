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
    public static final Set<String> OWN_KEYS = Set.of("SKULLAXE", "WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING",
        "MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE", "CLUSTER", "IMPACT", "VOIDTOUCHED");

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
            TcRecipes.register(new InfusionEnchantmentRecipe("CLUSTER", "CLUSTER", ForbiddenMagic.id("cluster"), 3,
                new AspectList().add(Aspect.FIRE, 4).add(Aspect.METAL, 4).add(Aspect.GREED, 4),
                List.of(
                    TcIngredient.item("thaumcraft:elemental_pickaxe"),
                    TcIngredient.item("thaumcraft:salis_mundus")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("IMPACT", "IMPACT", ForbiddenMagic.id("impact"), 4,
                new AspectList().add(Aspect.ENTROPY, 16).add(Aspect.MINE, 16),
                List.of(
                    TcIngredient.item("thaumcraft:salis_mundus"),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item("thaumcraft:elemental_shovel")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("VOIDTOUCHED", "VOIDTOUCHED", ForbiddenMagic.id("voidtouched"), 8,
                new AspectList().add(Aspect.VOID, 16).add(Aspect.DARKNESS, 16).add(FmAspects.ENVY, 24).add(Aspect.ELDRITCH, 16),
                List.of(
                    TcIngredient.item("thaumcraft:salis_mundus"),
                    TcIngredient.item("thaumcraft:void_seed"),
                    TcIngredient.item("thaumcraft:void_seed"),
                    TcIngredient.item("thaumcraft:void_seed"),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString())
                )));
        }
        registerMorphTools();
    }

    private static void registerMorphTools() {
        for (String[] tool : new String[][]{
            {"MORPHPICK", "morph_pickaxe", "thaumcraft:thaumium_pickaxe", "TOOL"},
            {"MORPHSWORD", "morph_sword", "thaumcraft:thaumium_sword", "WEAPON"},
            {"MORPHSHOVEL", "morph_shovel", "thaumcraft:thaumium_shovel", "TOOL"},
            {"MORPHAXE", "morph_axe", "thaumcraft:thaumium_axe", "TOOL"},
        }) {
            AspectList aspects = "WEAPON".equals(tool[3])
                ? new AspectList().add(Aspect.EXCHANGE, 32).add(FmAspects.ENVY, 16).add(Aspect.WEAPON, 16)
                : new AspectList().add(Aspect.EXCHANGE, 32).add(FmAspects.ENVY, 16).add(Aspect.TOOL, 16);
            TcRecipes.register(new InfusionRecipe(tool[0], "MORPHTOOLS",
                TcStack.of("forbiddenmagic:" + tool[1], 1), 6, aspects,
                TcIngredient.item(tool[2]),
                List.of(
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item("minecraft:diamond"),
                    TcIngredient.item("thaumcraft:enchanted_fabric"),
                    TcIngredient.item("thaumcraft:silverwood_log")
                )));
        }
    }
}

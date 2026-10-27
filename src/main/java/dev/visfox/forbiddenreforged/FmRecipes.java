package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.compat.crafting.CraftingRecipeRef;
import dev.visfox.forbiddenreforged.compat.crafting.CrucibleRecipe;
import dev.visfox.forbiddenreforged.compat.crafting.InfusionEnchantmentRecipe;
import dev.visfox.forbiddenreforged.compat.crafting.InfusionRecipe;
import dev.visfox.forbiddenreforged.compat.crafting.ShapedArcaneRecipe;
import dev.visfox.forbiddenreforged.compat.crafting.ShapelessArcaneRecipe;
import dev.visfox.forbiddenreforged.compat.crafting.TcIngredient;
import dev.visfox.forbiddenreforged.compat.crafting.TcRecipes;
import dev.visfox.forbiddenreforged.compat.crafting.TcStack;
import dev.visfox.forbiddenreforged.compat.research.ResearchConfig;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.util.List;
import java.util.Set;

public final class FmRecipes {
    public static final Set<String> OWN_KEYS =
            Set.of(
                    "SKULLAXE",
                    "FORK",
                    "WRATH",
                    "GREEDY",
                    "CONSUMING",
                    "EDUCATIONAL",
                    "CORRUPTING",
                    "MORPHPICK",
                    "MORPHSWORD",
                    "MORPHSHOVEL",
                    "MORPHAXE",
                    "CLUSTER",
                    "IMPACT",
                    "VOIDTOUCHED",
                    "TAINTTREE",
                    "BLACKFLOWER",
                    "ARCANECAKE",
                    "WRATHCAGE",
                    "MOBCRYSTAL",
                    "ROD_TAINTED",
                    "ROD_INFERNAL",
                    "TAINTSHOVEL",
                    "TAINTPICK",
                    "CRYSTALWELL",
                    "PRIMEWELL",
                    "TRANSEMERALD",
                    "FOCUSBLINK",
                    "RINGFOOD",
                    "TaintStone",
                    "TaintBrick",
                    "BlackInk",
                    "SUBCOLLAR");

    private FmRecipes() {}

    public static void register() {
        TcRecipes.register(
                new InfusionRecipe(
                        "SKULLAXE",
                        "SKULLAXE",
                        TcStack.of(FmItems.SKULL_AXE.getId().toString(), 1),
                        1,
                        new AspectList()
                                .add(FmAspects.WRATH, 8)
                                .add(Aspect.WEAPON, 8)
                                .add(FmAspects.NETHER, 8),
                        TcIngredient.item("tc4modern:thaumium_axe"),
                        List.of(
                                TcIngredient.item(FmItems.id("wrath").toString()),
                                TcIngredient.item(FmItems.id("wrath").toString()),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("minecraft:wither_skeleton_skull"))));
        TcRecipes.register(
                new InfusionRecipe(
                        "FORK",
                        "FORK",
                        TcStack.of(FmItems.DIABOLIST_FORK.getId().toString(), 1),
                        1,
                        new AspectList()
                                .add(FmAspects.NETHER, 8)
                                .add(Aspect.MECHANISM, 8)
                                .add(Aspect.ENERGY, 8),
                        TcIngredient.item("tc4modern:thaumium_sword"),
                        List.of(
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item("minecraft:redstone"))));
        if (FmConfig.enchantmentsEnabled()) {
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "WRATH",
                            "WRATH",
                            ForbiddenMagic.id("wrath"),
                            8,
                            new AspectList()
                                    .add(FmAspects.WRATH, 16)
                                    .add(Aspect.WEAPON, 16)
                                    .add(FmAspects.NETHER, 8),
                            List.of(
                                    TcIngredient.item("tc4modern:salis_mundus"),
                                    TcIngredient.item("minecraft:diamond_sword"),
                                    TcIngredient.item(FmItems.id("wrath").toString()),
                                    TcIngredient.item(FmItems.id("wrath").toString()),
                                    TcIngredient.item(FmItems.id("wrath").toString()))));
            if (FmConfig.greedyEnchantmentEnabled()) {
                TcRecipes.register(
                        new InfusionEnchantmentRecipe(
                                "GREEDY",
                                "GREEDY",
                                ForbiddenMagic.id("greedy"),
                                4,
                                new AspectList()
                                        .add(FmAspects.NETHER, 16)
                                        .add(Aspect.WEAPON, 8)
                                        .add(Aspect.GREED, 16),
                                List.of(
                                        TcIngredient.item("minecraft:golden_sword"),
                                        TcIngredient.item("minecraft:diamond"),
                                        TcIngredient.item(FmItems.id("greed").toString()),
                                        TcIngredient.item(FmItems.id("greed").toString()),
                                        TcIngredient.item("tc4modern:salis_mundus"))));
            }
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "CONSUMING",
                            "CONSUMING",
                            ForbiddenMagic.id("consuming"),
                            3,
                            new AspectList()
                                    .add(Aspect.VOID, 8)
                                    .add(Aspect.TOOL, 8)
                                    .add(Aspect.HUNGER, 8),
                            List.of(
                                    TcIngredient.item("minecraft:iron_pickaxe"),
                                    TcIngredient.item("minecraft:lava_bucket"),
                                    TcIngredient.item("tc4modern:salis_mundus"))));
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "EDUCATIONAL",
                            "EDUCATIONAL",
                            ForbiddenMagic.id("educational"),
                            3,
                            new AspectList()
                                    .add(Aspect.MAGIC, 4)
                                    .add(Aspect.WEAPON, 4)
                                    .add(Aspect.MIND, 8),
                            List.of(
                                    TcIngredient.item("tc4modern:zombie_brain"),
                                    TcIngredient.item("minecraft:book"),
                                    TcIngredient.item("tc4modern:salis_mundus"))));
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "CORRUPTING",
                            "CORRUPTING",
                            ForbiddenMagic.id("corrupting"),
                            4,
                            new AspectList()
                                    .add(FmAspects.NETHER, 16)
                                    .add(Aspect.EXCHANGE, 16)
                                    .add(Aspect.CRYSTAL, 8),
                            List.of(
                                    TcIngredient.item("minecraft:nether_wart"),
                                    TcIngredient.item("minecraft:soul_sand"),
                                    TcIngredient.item("tc4modern:salis_mundus"))));
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "CLUSTER",
                            "CLUSTER",
                            ForbiddenMagic.id("cluster"),
                            3,
                            new AspectList()
                                    .add(Aspect.FIRE, 4)
                                    .add(Aspect.METAL, 4)
                                    .add(Aspect.GREED, 4),
                            List.of(
                                    TcIngredient.item("tc4modern:elemental_pickaxe"),
                                    TcIngredient.item("tc4modern:salis_mundus"))));
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "IMPACT",
                            "IMPACT",
                            ForbiddenMagic.id("impact"),
                            4,
                            new AspectList().add(Aspect.ENTROPY, 16).add(Aspect.MINE, 16),
                            List.of(
                                    TcIngredient.item("tc4modern:salis_mundus"),
                                    TcIngredient.item(FmItems.id("envy").toString()),
                                    TcIngredient.item(FmItems.id("envy").toString()),
                                    TcIngredient.item("tc4modern:elemental_shovel"))));
            TcRecipes.register(
                    new InfusionEnchantmentRecipe(
                            "VOIDTOUCHED",
                            "VOIDTOUCHED",
                            ForbiddenMagic.id("voidtouched"),
                            8,
                            new AspectList()
                                    .add(Aspect.VOID, 16)
                                    .add(Aspect.DARKNESS, 16)
                                    .add(FmAspects.ENVY, 24)
                                    .add(Aspect.ELDRITCH, 16),
                            List.of(
                                    TcIngredient.item("tc4modern:salis_mundus"),
                                    TcIngredient.item("tc4modern:void_seed"),
                                    TcIngredient.item("tc4modern:void_seed"),
                                    TcIngredient.item("tc4modern:void_seed"),
                                    TcIngredient.item(FmItems.id("envy").toString()),
                                    TcIngredient.item(FmItems.id("envy").toString()),
                                    TcIngredient.item(FmItems.id("envy").toString()))));
        }
        registerMorphTools();
        if (FmConfig.emeraldTrans()) {
            TcRecipes.register(
                    new CrucibleRecipe(
                            "TRANSEMERALD",
                            "TRANSEMERALD",
                            TcStack.of(FmItems.EMERALD_FRAGMENT.getId().toString(), 4),
                            TcIngredient.tag("c:nuggets/emerald"),
                            new AspectList().add(Aspect.CRYSTAL, 2).add(Aspect.GREED, 2)));
        }
        if (FmConfig.gluttonyMode() != 1) {
            List<TcIngredient> cakeParts =
                    FmConfig.gluttonyMode() == 2
                            ? List.of(
                                    TcIngredient.item("tc4modern:salis_mundus"),
                                    TcIngredient.item("forbiddenmagic:star_block"),
                                    TcIngredient.item("forbiddenmagic:star_block"),
                                    TcIngredient.item("minecraft:egg"),
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()),
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()))
                            : List.of(
                                    TcIngredient.item("tc4modern:salis_mundus"),
                                    TcIngredient.item("minecraft:egg"),
                                    TcIngredient.item("minecraft:milk_bucket"),
                                    TcIngredient.item("minecraft:egg"),
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()),
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()));
            TcRecipes.register(
                    new InfusionRecipe(
                            "ARCANECAKE",
                            "ARCANECAKE",
                            TcStack.of("forbiddenmagic:arcane_cake", 1),
                            3,
                            new AspectList()
                                    .add(FmAspects.GLUTTONY, 12)
                                    .add(Aspect.HUNGER, 24)
                                    .add(Aspect.CRAFT, 24),
                            TcIngredient.item("minecraft:cake"),
                            cakeParts));
        }
        TcRecipes.register(
                new CrucibleRecipe(
                        "TAINTTREE",
                        "TAINTTREE",
                        TcStack.of("forbiddenmagic:taint_sapling", 1),
                        TcIngredient.item("minecraft:oak_sapling"),
                        new AspectList().add(Aspect.TAINT, 10).add(Aspect.POISON, 4)));
        TcRecipes.register(
                new CrucibleRecipe(
                        "BLACKFLOWER",
                        "BLACKFLOWER",
                        TcStack.of("forbiddenmagic:umbral_rose_bush", 1),
                        TcIngredient.item("minecraft:rose_bush"),
                        new AspectList().add(Aspect.DARKNESS, 8).add(Aspect.LIFE, 5)));
        TcRecipes.register(
                new CraftingRecipeRef(
                        "BlackInk",
                        "BLACKFLOWER",
                        ForbiddenMagic.id("ink_from_rose"),
                        TcStack.of("forbiddenmagic:black_dye", 2)));
        if (!FmConfig.lustDisabled()) {
            TcRecipes.register(
                    new InfusionRecipe(
                            "SUBCOLLAR",
                            "SUBCOLLAR",
                            TcStack.of("forbiddenmagic:sub_collar", 1),
                            1,
                            new AspectList()
                                    .add(FmAspects.LUST, 8)
                                    .add(Aspect.TRAP, 8)
                                    .add(FmAspects.NETHER, 4)
                                    .add(Aspect.FLESH, 4),
                            TcIngredient.item("tc4modern:vis_amulet"),
                            List.of(
                                    TcIngredient.item(FmItems.id("lust").toString()),
                                    TcIngredient.item(FmItems.id("lust").toString()),
                                    TcIngredient.item(FmItems.id("lust").toString()),
                                    TcIngredient.item("minecraft:lead"),
                                    TcIngredient.item("tc4modern:mundane_belt"))));
        }
        TcRecipes.register(
                new ShapedArcaneRecipe(
                        "TaintStone",
                        "TAINTSTONE",
                        TcStack.of("forbiddenmagic:taint_stone_smooth", 9),
                        new AspectList().add(Aspect.ENTROPY, 2).add(Aspect.ORDER, 1),
                        3,
                        3,
                        List.of(
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.tag("forbiddenmagic:taint_ingredients"),
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.item("minecraft:stone"),
                                TcIngredient.item("minecraft:stone"))));
        TcRecipes.register(
                new CraftingRecipeRef(
                        "TaintBrick",
                        "TAINTSTONE",
                        ForbiddenMagic.id("taint_stone_brick"),
                        TcStack.of("forbiddenmagic:taint_stone_brick", 4)));
        TcRecipes.register(
                new InfusionRecipe(
                        "WRATHCAGE",
                        "WRATHCAGE",
                        TcStack.of("forbiddenmagic:wrath_cage", 1),
                        10,
                        new AspectList()
                                .add(FmAspects.WRATH, 32)
                                .add(Aspect.MAGIC, 32)
                                .add(Aspect.BEAST, 32)
                                .add(Aspect.MECHANISM, 16),
                        TcIngredient.item("tc4modern:thaumium_block"),
                        List.of(
                                TcIngredient.item(FmItems.id("wrath").toString()),
                                TcIngredient.item(FmItems.id("wrath").toString()),
                                TcIngredient.item(FmItems.id("wrath").toString()),
                                TcIngredient.item(FmItems.id("wrath").toString()),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("tc4modern:warded_jar"),
                                TcIngredient.item("tc4modern:warded_jar"),
                                TcIngredient.item("tc4modern:warded_jar"))));
        TcRecipes.register(
                new CrucibleRecipe(
                        "MOBCRYSTAL",
                        "WRATHCAGE",
                        TcStack.of("forbiddenmagic:mob_crystal", 1),
                        TcIngredient.item("minecraft:diamond"),
                        new AspectList().add(Aspect.MIND, 10).add(Aspect.ENERGY, 10)));
        TcRecipes.register(
                new InfusionRecipe(
                        "ROD_TAINTED",
                        "ROD_TAINTED",
                        TcStack.of(FmItems.TAINTED_ROD.getId().toString(), 1),
                        5,
                        new AspectList()
                                .add(Aspect.TAINT, 24)
                                .add(Aspect.MAGIC, 12)
                                .add(Aspect.ENTROPY, 12),
                        TcIngredient.item("tc4modern:tainted_goo"),
                        List.of(
                                TcIngredient.item("tc4modern:salis_mundus"),
                                TcIngredient.item(FmItems.id("taint").toString()),
                                TcIngredient.item(FmItems.id("taint").toString()),
                                TcIngredient.item(FmItems.id("taint").toString()),
                                TcIngredient.item(FmItems.id("taint").toString()),
                                TcIngredient.item(FmItems.id("taint").toString()),
                                TcIngredient.item(FmItems.id("taint").toString()))));
        TcRecipes.register(
                new InfusionRecipe(
                        "ROD_INFERNAL",
                        "ROD_INFERNAL",
                        TcStack.of(FmItems.INFERNAL_ROD.getId().toString(), 1),
                        5,
                        new AspectList()
                                .add(FmAspects.NETHER, 32)
                                .add(Aspect.MAGIC, 12)
                                .add(FmAspects.PRIDE, 12),
                        TcIngredient.item("minecraft:blaze_rod"),
                        List.of(
                                TcIngredient.item("tc4modern:salis_mundus"),
                                TcIngredient.item(FmItems.id("pride").toString()),
                                TcIngredient.item(FmItems.id("pride").toString()),
                                TcIngredient.item("minecraft:soul_sand"),
                                TcIngredient.item("minecraft:wither_skeleton_skull"),
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item("minecraft:blaze_powder"))));
        TcRecipes.register(
                new InfusionRecipe(
                        "TAINTSHOVEL",
                        "TAINTSHOVEL",
                        TcStack.of(FmItems.TAINT_SHOVEL.getId().toString(), 1),
                        5,
                        new AspectList()
                                .add(Aspect.HEAL, 8)
                                .add(Aspect.CRYSTAL, 24)
                                .add(Aspect.MINE, 12),
                        TcIngredient.item("tc4modern:thaumium_shovel"),
                        List.of(
                                TcIngredient.item("tc4modern:balanced_shard"),
                                TcIngredient.item("tc4modern:balanced_shard"),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("tc4modern:silverwood_log"))));
        TcRecipes.register(
                new InfusionRecipe(
                        "TAINTPICK",
                        "TAINTPICK",
                        TcStack.of(FmItems.TAINT_PICKAXE.getId().toString(), 1),
                        1,
                        new AspectList()
                                .add(Aspect.ENTROPY, 8)
                                .add(Aspect.MINE, 8)
                                .add(Aspect.TAINT, 12),
                        TcIngredient.item("tc4modern:thaumium_pickaxe"),
                        List.of(
                                TcIngredient.item("tc4modern:entropy_shard"),
                                TcIngredient.item("tc4modern:entropy_shard"),
                                TcIngredient.item("minecraft:diamond"),
                                TcIngredient.item("tc4modern:greatwood_log"))));
        if (ResearchConfig.difficulty() == -1) {
            TcRecipes.register(
                    new ShapelessArcaneRecipe(
                            "CRYSTALWELL",
                            "CRYSTALWELL",
                            TcStack.of(FmItems.CRYSTALWELL.getId().toString(), 1),
                            new AspectList().add(Aspect.WATER, 1).add(Aspect.ORDER, 1),
                            List.of(
                                    TcIngredient.item("tc4modern:scribing_tools"),
                                    TcIngredient.tag("c:dyes/black"),
                                    TcIngredient.tag("tc4modern:shards"),
                                    TcIngredient.tag("tc4modern:shards"))));
        } else {
            TcRecipes.register(
                    new ShapelessArcaneRecipe(
                            "CRYSTALWELL",
                            "CRYSTALWELL",
                            TcStack.of(FmItems.CRYSTALWELL.getId().toString(), 1),
                            new AspectList().add(Aspect.WATER, 1).add(Aspect.ORDER, 1),
                            List.of(
                                    TcIngredient.item("tc4modern:scribing_tools"),
                                    TcIngredient.tag("c:dyes/black"),
                                    TcIngredient.item("tc4modern:mixed_crystal_cluster"),
                                    TcIngredient.item("tc4modern:balanced_shard"),
                                    TcIngredient.item("tc4modern:balanced_shard"),
                                    TcIngredient.item("tc4modern:balanced_shard"))));
        }
        TcRecipes.register(
                new ShapelessArcaneRecipe(
                        "PRIMEWELL",
                        "PRIMEWELL",
                        TcStack.of(FmItems.PRIMEWELL.getId().toString(), 1),
                        new AspectList()
                                .add(Aspect.WATER, 50)
                                .add(Aspect.EARTH, 50)
                                .add(Aspect.FIRE, 50)
                                .add(Aspect.AIR, 50)
                                .add(Aspect.ORDER, 50)
                                .add(Aspect.ENTROPY, 50),
                        List.of(
                                TcIngredient.item("minecraft:feather"),
                                TcIngredient.item("tc4modern:primordial_pearl"),
                                TcIngredient.item("minecraft:glass_bottle"))));
        if (FmConfig.gluttonyMode() != 1) {
            String ringFoodTopIngredient =
                    FmConfig.gluttonyMode() == 2
                            ? "forbiddenmagic:star_block"
                            : "minecraft:diamond";
            TcRecipes.register(
                    new ShapedArcaneRecipe(
                            "RINGFOOD",
                            "RINGFOOD",
                            TcStack.of(FmItems.RING_NUTRITION.getId().toString(), 1),
                            new AspectList()
                                    .add(Aspect.ENTROPY, 10)
                                    .add(Aspect.WATER, 20)
                                    .add(Aspect.EARTH, 20),
                            3,
                            3,
                            List.of(
                                    TcIngredient.EMPTY,
                                    TcIngredient.item(ringFoodTopIngredient),
                                    TcIngredient.EMPTY,
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()),
                                    TcIngredient.item("tc4modern:mundane_ring"),
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()),
                                    TcIngredient.EMPTY,
                                    TcIngredient.item(FmItems.GLUTTONY_SHARD.getId().toString()),
                                    TcIngredient.EMPTY)));
        }
        TcRecipes.register(
                new InfusionRecipe(
                        "FOCUSBLINK",
                        "FOCUSBLINK",
                        TcStack.of(FmItems.BLINK_FOCUS.getId().toString(), 1),
                        3,
                        new AspectList()
                                .add(Aspect.TRAVEL, 25)
                                .add(FmAspects.NETHER, 10)
                                .add(FmAspects.SLOTH, 10)
                                .add(Aspect.ENTROPY, 25),
                        TcIngredient.item("minecraft:ender_pearl"),
                        List.of(
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item(FmItems.id("sloth").toString()),
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item(FmItems.id("sloth").toString()),
                                TcIngredient.item("minecraft:quartz"),
                                TcIngredient.item(FmItems.id("sloth").toString()))));
    }

    private static void registerMorphTools() {
        for (String[] tool :
                new String[][] {
                    {"MORPHPICK", "morph_pickaxe", "tc4modern:thaumium_pickaxe", "TOOL"},
                    {"MORPHSWORD", "morph_sword", "tc4modern:thaumium_sword", "WEAPON"},
                    {"MORPHSHOVEL", "morph_shovel", "tc4modern:thaumium_shovel", "TOOL"},
                    {"MORPHAXE", "morph_axe", "tc4modern:thaumium_axe", "TOOL"},
                }) {
            AspectList aspects =
                    "WEAPON".equals(tool[3])
                            ? new AspectList()
                                    .add(Aspect.EXCHANGE, 32)
                                    .add(FmAspects.ENVY, 16)
                                    .add(Aspect.WEAPON, 16)
                            : new AspectList()
                                    .add(Aspect.EXCHANGE, 32)
                                    .add(FmAspects.ENVY, 16)
                                    .add(Aspect.TOOL, 16);
            TcRecipes.register(
                    new InfusionRecipe(
                            tool[0],
                            "MORPHTOOLS",
                            TcStack.of("forbiddenmagic:" + tool[1], 1),
                            6,
                            aspects,
                            TcIngredient.item(tool[2]),
                            List.of(
                                    TcIngredient.item(FmItems.id("envy").toString()),
                                    TcIngredient.item(FmItems.id("envy").toString()),
                                    TcIngredient.item("minecraft:diamond"),
                                    TcIngredient.item("tc4modern:enchanted_fabric"),
                                    TcIngredient.item("tc4modern:silverwood_log"))));
        }
    }
}

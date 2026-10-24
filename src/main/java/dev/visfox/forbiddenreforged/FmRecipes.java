package dev.visfox.forbiddenreforged;

import java.util.List;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;

public final class FmRecipes {
    private FmRecipes() {
    }

    public static void register() {

        TcRecipes.register(new InfusionRecipe(
            "FM_ROD_TAINTED",
            "ROD_tainted",
            TcStack.of("forbiddenmagic:tainted_rod"),
            5,
            new AspectList().add(Aspect.TAINT, 24).add(Aspect.MAGIC, 12).add(Aspect.ENTROPY, 12),
            TcIngredient.item("thaumcraft:tainted_goo"),
            List.of(
                TcIngredient.item("thaumcraft:salis_mundus"),
                TcIngredient.item("forbiddenmagic:taint_shard"),
                TcIngredient.item("forbiddenmagic:taint_shard"),
                TcIngredient.item("forbiddenmagic:taint_shard"),
                TcIngredient.item("forbiddenmagic:taint_shard"),
                TcIngredient.item("forbiddenmagic:taint_shard"),
                TcIngredient.item("forbiddenmagic:taint_shard")
            )
        ));

        TcRecipes.register(new InfusionRecipe(
            "FM_ROD_INFERNAL",
            "ROD_infernal",
            TcStack.of("forbiddenmagic:infernal_rod"),
            5,
            new AspectList().add(FmAspects.NETHER, 32).add(Aspect.MAGIC, 12).add(FmAspects.PRIDE, 12),
            TcIngredient.item("minecraft:blaze_rod"),
            List.of(
                TcIngredient.item("thaumcraft:salis_mundus"),
                TcIngredient.item("forbiddenmagic:pride_shard"),
                TcIngredient.item("forbiddenmagic:pride_shard"),
                TcIngredient.item("minecraft:soul_sand"),
                TcIngredient.item("minecraft:wither_skeleton_skull"),
                TcIngredient.item("minecraft:quartz"),
                TcIngredient.item("minecraft:blaze_powder")
            )
        ));

        TcRecipes.register(new CraftingRecipeRef(
            "BlackInk",
            "BLACKFLOWER",
            ResourceLocation.parse("forbiddenmagic:ink_from_rose"),
            TcStack.of("forbiddenmagic:black_dye", 2)
        ));

        TcRecipes.register(new CraftingRecipeRef(
            "TaintBrick",
            "TAINTSTONE",
            ResourceLocation.parse("forbiddenmagic:taint_stone_brick"),
            TcStack.of("forbiddenmagic:taint_stone_brick", 4)
        ));

        // Original Forbidden Magic crucible conversion: oak sapling -> taint sapling.
        TcRecipes.register(new CrucibleRecipe(
            "FM_TAINTTREE",
            "TAINTTREE",
            TcStack.of("forbiddenmagic:taint_sapling"),
            TcIngredient.item("minecraft:oak_sapling"),
            new AspectList().add(Aspect.TAINT, 10).add(Aspect.POISON, 4)
        ));

        // Original black-flower research converts a vanilla rose bush into the umbral rose bush.
        TcRecipes.register(new CrucibleRecipe(
            "FM_BLACKFLOWER",
            "BLACKFLOWER",
            TcStack.of("forbiddenmagic:umbral_rose_bush"),
            TcIngredient.item("minecraft:rose_bush"),
            new AspectList().add(Aspect.DARKNESS, 8).add(Aspect.LIFE, 5)
        ));

        // Tainted stone recipe uses the Thaumcraft arcane crafting station, not a vanilla crafting grid.
        TcRecipes.register(new ShapedArcaneRecipe(
            "FM_TAINTSTONE",
            "TAINTSTONE",
            TcStack.of("forbiddenmagic:taint_stone_smooth", 9),
            new AspectList().add(Aspect.ENTROPY, 2).add(Aspect.ORDER, 1),
            3,
            3,
            List.of(
                TcIngredient.item("minecraft:stone"), TcIngredient.item("minecraft:stone"), TcIngredient.item("minecraft:stone"),
                TcIngredient.item("minecraft:stone"), TcIngredient.item("forbiddenmagic:taint_shard"), TcIngredient.item("minecraft:stone"),
                TcIngredient.item("minecraft:stone"), TcIngredient.item("minecraft:stone"), TcIngredient.item("minecraft:stone")
            )
        ));

        // Preserve the standard Thaumic Cake infusion recipe from the 1.7.10-era design.
        TcRecipes.register(new InfusionRecipe(
            "FM_ARCANECAKE",
            "ARCANECAKE",
            TcStack.of("forbiddenmagic:arcane_cake"),
            3,
            new AspectList().add(FmAspects.GLUTTONY, 12).add(Aspect.HUNGER, 24).add(Aspect.CRAFT, 24),
            TcIngredient.item("minecraft:cake"),
            List.of(
                TcIngredient.item("thaumcraft:salis_mundus"),
                TcIngredient.item("minecraft:egg"),
                TcIngredient.item("minecraft:milk_bucket"),
                TcIngredient.item("minecraft:egg"),
                TcIngredient.item("forbiddenmagic:gluttony_shard"),
                TcIngredient.item("forbiddenmagic:gluttony_shard")
            )
        ));
    }
}

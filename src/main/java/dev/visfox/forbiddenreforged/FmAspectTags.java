package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.compat.aspect.AspectRegistrar;
import dev.visfox.forbiddenreforged.compat.aspect.AspectRegistry;
import dev.visfox.forbiddenreforged.compat.aspect.AspectSync;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public final class FmAspectTags {
    private FmAspectTags() {}

    public static void register() {
        AspectSync.addRegistrar(registry -> new Table(registry).registerAll());
    }

    private static final class Table extends AspectRegistrar {
        private Table(AspectRegistry registry) {
            super(registry);
        }

        private void registerAll() {
            registerShard(
                    "wrath",
                    tags().add(FmAspects.NETHER, 1).add(FmAspects.WRATH, 2).add(Aspect.CRYSTAL, 1));
            registerShard(
                    "envy",
                    tags().add(FmAspects.NETHER, 1).add(FmAspects.ENVY, 2).add(Aspect.CRYSTAL, 1));
            registerShard("taint", tags().add(Aspect.TAINT, 3).add(Aspect.CRYSTAL, 1));
            registerShard(
                    "pride",
                    tags().add(FmAspects.NETHER, 1).add(FmAspects.PRIDE, 2).add(Aspect.CRYSTAL, 1));
            registerShard(
                    "sloth",
                    tags().add(FmAspects.NETHER, 1).add(FmAspects.SLOTH, 2).add(Aspect.CRYSTAL, 1));
            registerShard(
                    "greed",
                    tags().add(FmAspects.NETHER, 1).add(Aspect.GREED, 2).add(Aspect.CRYSTAL, 1));
            if (!FmConfig.lustDisabled()) {
                registerShard(
                        "lust",
                        tags().add(FmAspects.NETHER, 1)
                                .add(FmAspects.LUST, 2)
                                .add(Aspect.CRYSTAL, 1));
            } else {
                registerShard("lust", tags().add(Aspect.VOID, 1));
            }

            register(
                    "forbiddenmagic:gluttony_shard",
                    tags().add(FmAspects.NETHER, 1)
                            .add(FmAspects.GLUTTONY, 2)
                            .add(Aspect.CRYSTAL, 1));
            register("forbiddenmagic:emerald_fragment", tags().add(Aspect.CRYSTAL, 1));
            register("forbiddenmagic:black_dye", tags().add(Aspect.DARKNESS, 1));
            register(
                    "forbiddenmagic:taint_coal",
                    tags().add(Aspect.TAINT, 2).add(Aspect.ENTROPY, 2));
            register(
                    "forbiddenmagic:taint_fruit",
                    tags().add(Aspect.TAINT, 2).add(Aspect.POISON, 1));

            // Blocks are tagged through their BlockItem identifiers in Thaumcraft's item-aspect
            // registry.
            register("forbiddenmagic:taint_log", tags().add(Aspect.TREE, 4).add(Aspect.TAINT, 2));
            register(
                    "forbiddenmagic:taint_planks", tags().add(Aspect.TREE, 1).add(Aspect.TAINT, 1));
            register(
                    "forbiddenmagic:taint_stone_smooth",
                    tags().add(Aspect.EARTH, 2).add(Aspect.TAINT, 2));
            register(
                    "forbiddenmagic:taint_stone_brick",
                    tags().add(Aspect.EARTH, 2).add(Aspect.TAINT, 2));
            register(
                    "forbiddenmagic:star_block",
                    tags().add(Aspect.MAGIC, 8).add(Aspect.CRYSTAL, 8));
            register(
                    "forbiddenmagic:taint_leaves",
                    tags().add(Aspect.PLANT, 2).add(Aspect.TAINT, 2));
            register(
                    "forbiddenmagic:taint_sapling",
                    tags().add(Aspect.PLANT, 2).add(Aspect.TREE, 1).add(Aspect.TAINT, 2));
            register(
                    "forbiddenmagic:umbral_rose",
                    tags().add(Aspect.PLANT, 2).add(Aspect.DARKNESS, 2));
            register(
                    "forbiddenmagic:umbral_rose_bush",
                    tags().add(Aspect.PLANT, 3).add(Aspect.DARKNESS, 3));
            register(
                    "forbiddenmagic:arcane_cake",
                    tags().add(Aspect.HUNGER, 4).add(FmAspects.GLUTTONY, 4).add(Aspect.CRAFT, 2));
            register(
                    "forbiddenmagic:mob_crystal", tags().add(Aspect.MIND, 2).add(Aspect.ENERGY, 2));
            register(
                    "forbiddenmagic:diabolist_fork",
                    tags().add(Aspect.WEAPON, 2).add(Aspect.MECHANISM, 1));
            register(
                    "forbiddenmagic:wrath_cage",
                    tags().add(FmAspects.WRATH, 4).add(Aspect.BEAST, 4).add(Aspect.MECHANISM, 4));
        }

        private void registerShard(String vice, AspectList aspects) {
            register("forbiddenmagic:" + vice + "_shard", aspects);
        }
    }
}

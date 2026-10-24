package dev.visfox.forbiddenreforged;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistrar;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.aspect.AspectSync;

public final class FmAspectTags {
    private FmAspectTags() {
    }

    public static void register() {
        AspectSync.addRegistrar(registry -> new Table(registry).registerAll());
    }

    private static final class Table extends AspectRegistrar {
        private Table(AspectRegistry registry) {
            super(registry);
        }

        private void registerAll() {
            registerShard("wrath", tags().add(FmAspects.NETHER, 1).add(FmAspects.WRATH, 2).add(Aspect.CRYSTAL, 1));
            registerShard("envy", tags().add(FmAspects.NETHER, 1).add(FmAspects.ENVY, 2).add(Aspect.CRYSTAL, 1));
            registerShard("taint", tags().add(Aspect.TAINT, 3).add(Aspect.CRYSTAL, 1));
            registerShard("pride", tags().add(FmAspects.NETHER, 1).add(FmAspects.PRIDE, 2).add(Aspect.CRYSTAL, 1));
            registerShard("sloth", tags().add(FmAspects.NETHER, 1).add(FmAspects.SLOTH, 2).add(Aspect.CRYSTAL, 1));
            registerShard("greed", tags().add(FmAspects.NETHER, 1).add(Aspect.GREED, 2).add(Aspect.CRYSTAL, 1));
            if (!FmConfig.lustDisabled()) {
                registerShard("lust", tags().add(FmAspects.NETHER, 1).add(FmAspects.LUST, 2).add(Aspect.CRYSTAL, 1));
            } else {
                registerShard("lust", tags().add(Aspect.VOID, 1));
            }

            register("forbiddenmagic:gluttony_shard",
                tags().add(FmAspects.NETHER, 1).add(FmAspects.GLUTTONY, 2).add(Aspect.CRYSTAL, 1));
            register("forbiddenmagic:emerald_fragment", tags().add(Aspect.CRYSTAL, 1));
            register("forbiddenmagic:black_dye", tags().add(Aspect.DARKNESS, 1));
            register("forbiddenmagic:taint_coal", tags().add(Aspect.TAINT, 2).add(Aspect.ENTROPY, 2));
            register("forbiddenmagic:taint_fruit", tags().add(Aspect.TAINT, 2).add(Aspect.POISON, 1));
        }

        private void registerShard(String vice, AspectList aspects) {
            register("forbiddenmagic:" + vice + "_shard", aspects);
        }
    }
}

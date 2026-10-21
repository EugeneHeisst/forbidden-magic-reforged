package dev.visfox.forbiddenreforged;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistrar;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.aspect.AspectSync;

public final class FmAspectTags {
    private static final Set<String> DECLARED = new LinkedHashSet<>();

    private FmAspectTags() {
    }

    public static void register() {
        AspectSync.addRegistrar(registry -> new Table(registry).apply());
    }

    public static List<String> declaredTargets() {
        return List.copyOf(DECLARED);
    }

    private static final class Table extends AspectRegistrar {
        Table(AspectRegistry registry) {
            super(registry);
        }

        void apply() {
            DECLARED.clear();
            merge("minecraft:netherrack", tags().add(FmAspects.NETHER, 1));
            merge("minecraft:obsidian", tags().add(FmAspects.NETHER, 4));
            DECLARED.add("#c:ores/quartz");
            register("#c:ores/quartz", tags().add(Aspect.CRYSTAL, 3).add(FmAspects.NETHER, 2));
            merge("minecraft:nether_star", tags().add(FmAspects.NETHER, 8).add(FmAspects.PRIDE, 8));
            merge("minecraft:nether_wart", tags().add(FmAspects.NETHER, 1));
            merge("minecraft:nether_brick", tags().add(FmAspects.NETHER, 1));
            merge("minecraft:nether_bricks", tags().add(FmAspects.NETHER, 2));
            merge("minecraft:ghast_tear", tags().add(FmAspects.WRATH, 4));
            merge("minecraft:wither_skeleton_skull", tags().add(FmAspects.NETHER, 4));
            merge("minecraft:creeper_head", tags().add(FmAspects.WRATH, 2));
            merge("minecraft:tnt", tags().add(FmAspects.WRATH, 2));
            merge("minecraft:golden_helmet", tags().add(FmAspects.PRIDE, 2));
            merge("minecraft:golden_chestplate", tags().add(FmAspects.PRIDE, 1));
            merge("minecraft:golden_leggings", tags().add(FmAspects.PRIDE, 1));
            merge("minecraft:golden_boots", tags().add(FmAspects.PRIDE, 1));
            merge("minecraft:golden_sword", tags().add(FmAspects.PRIDE, 2));
            DECLARED.add("#minecraft:beds");
            register("#minecraft:beds", tags().add(Aspect.CRAFT, 3).add(Aspect.CLOTH, 6).add(FmAspects.SLOTH, 4));
            DECLARED.add("minecraft:ender_eye");
            register("minecraft:ender_eye", tags().add(Aspect.SENSES, 4).add(Aspect.ELDRITCH, 4).add(Aspect.MAGIC, 3).add(FmAspects.ENVY, 4));
            merge("minecraft:cake", tags().add(FmAspects.GLUTTONY, 7));
            merge("minecraft:cookie", tags().add(FmAspects.GLUTTONY, 1));
            merge("minecraft:fire_charge", tags().add(FmAspects.WRATH, 1));
            DECLARED.add("minecraft:comparator");
            register("minecraft:comparator", tags().add(Aspect.MECHANISM, 2).add(Aspect.ORDER, 2).add(FmAspects.ENVY, 2));
            if (!FmConfig.lustDisabled()) {
                merge("minecraft:saddle", tags().add(FmAspects.LUST, 2));
                DECLARED.add("minecraft:lead");
                register("minecraft:lead", tags().add(Aspect.BEAST, 2).add(Aspect.CLOTH, 2).add(Aspect.SLIME, 1).add(FmAspects.LUST, 2));
            }
            addShard("wrath", tags().add(FmAspects.NETHER, 1).add(FmAspects.WRATH, 2).add(Aspect.CRYSTAL, 1));
            addShard("envy", tags().add(FmAspects.NETHER, 1).add(FmAspects.ENVY, 2).add(Aspect.CRYSTAL, 1));
            addShard("taint", tags().add(Aspect.TAINT, 3).add(Aspect.CRYSTAL, 1));
            addShard("pride", tags().add(FmAspects.NETHER, 1).add(FmAspects.PRIDE, 2).add(Aspect.CRYSTAL, 1));
            if (!FmConfig.lustDisabled()) {
                addShard("lust", tags().add(FmAspects.NETHER, 1).add(FmAspects.LUST, 2).add(Aspect.CRYSTAL, 1));
            }
            addShard("sloth", tags().add(FmAspects.NETHER, 1).add(FmAspects.SLOTH, 2).add(Aspect.CRYSTAL, 1));
            addShard("greed", tags().add(FmAspects.NETHER, 1).add(Aspect.GREED, 2).add(Aspect.CRYSTAL, 1));
            String gluttony = FmItems.GLUTTONY_SHARD.getId().toString();
            DECLARED.add(gluttony);
            register(gluttony, tags().add(FmAspects.NETHER, 1).add(FmAspects.GLUTTONY, 2).add(Aspect.CRYSTAL, 1));
            addEntity("thaumcraft:brainy_zombie", tags().add(FmAspects.WRATH, 3));
            addEntity("thaumcraft:giant_brainy_zombie", tags().add(FmAspects.WRATH, 4));
            addEntity("thaumcraft:firebat", tags().add(FmAspects.WRATH, 1).add(FmAspects.NETHER, 2));
            addEntity("minecraft:wither_skeleton", tags().add(FmAspects.NETHER, 3));
            addEntity("minecraft:creeper", tags().add(FmAspects.WRATH, 2));
            addEntity("minecraft:creeper", tags().add(FmAspects.WRATH, 4), "powered", 1);
            addEntity("minecraft:wither", tags().add(FmAspects.NETHER, 7).add(FmAspects.WRATH, 7));
            addEntity("minecraft:ocelot", tags().add(FmAspects.SLOTH, 3));
            addEntity("minecraft:enderman", tags().add(FmAspects.PRIDE, 4).add(FmAspects.ENVY, 4));
            addEntity("minecraft:ghast", tags().add(FmAspects.NETHER, 3).add(FmAspects.WRATH, 3));
            addEntity("minecraft:zombified_piglin", tags().add(FmAspects.WRATH, 6));
            addEntity("minecraft:ender_dragon", tags().add(FmAspects.PRIDE, 10));
            if (!FmConfig.lustDisabled()) {
                addEntity("thaumcraft:taintacle", tags().add(FmAspects.LUST, 3));
            }
            addEntity("minecraft:pig", tags().add(FmAspects.GLUTTONY, 3));
            addEntity("minecraft:magma_cube", tags().add(FmAspects.NETHER, 1));
        }

        private void merge(String target, AspectList additions) {
            DECLARED.add(target);
            AspectList merged = copy(target);
            for (Aspect aspect : additions.getAspects()) {
                merged.add(aspect, additions.getAmount(aspect));
            }
            register(target, merged);
        }

        private void addShard(String vice, AspectList aspects) {
            String target = FmItems.DEADLY_SHARDS.get(vice).getId().toString();
            DECLARED.add(target);
            register(target, aspects);
        }

        private void addEntity(String target, AspectList additions, Object... nbt) {
            DECLARED.add("entity:" + target);
            entityCopy(target, additions, nbt);
        }
    }
}

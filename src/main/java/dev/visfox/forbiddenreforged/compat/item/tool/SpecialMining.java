package dev.visfox.forbiddenreforged.compat.item.tool;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class SpecialMining {
    public static ItemStack find(
            BlockState state, ItemStack drop, float chance, RandomSource random) {
        var cluster = dev.eugheist.tc4modern.api.TC4Addons.nativeCluster(drop);
        if (cluster != null && random.nextFloat() < chance * cluster.factor())
            return new ItemStack(cluster.output().get(), cluster.perBlock() ? 1 : drop.getCount());
        String blockId =
                net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getKey(state.getBlock())
                        .toString();
        if ((blockId.equals("tc4modern:cinnabar_ore")
                        || blockId.equals("tc4modern:deepslate_cinnabar_ore"))
                && random.nextFloat() < chance * .9F)
            return new ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                            net.minecraft.resources.ResourceLocation.parse(
                                    "tc4modern:cinnabar_cluster")),
                    drop.getCount());
        String itemId =
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(drop.getItem())
                        .getNamespace();
        if (!itemId.equals("minecraft")) return drop;
        String path =
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(drop.getItem())
                        .getPath();
        String metal =
                switch (path) {
                    case "raw_iron", "iron_ore", "deepslate_iron_ore" -> "iron";
                    case "raw_gold", "gold_ore", "deepslate_gold_ore", "nether_gold_ore" -> "gold";
                    case "raw_copper", "copper_ore", "deepslate_copper_ore" -> "copper";
                    default -> "";
                };
        if (!metal.isEmpty() && random.nextFloat() < chance * (metal.equals("gold") ? .9F : 1F)) {
            var id =
                    net.minecraft.resources.ResourceLocation.parse(
                            "tc4modern:" + metal + "_cluster");
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id))
                return new ItemStack(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id),
                        metal.equals("copper") ? 1 : drop.getCount());
        }
        return drop;
    }
}

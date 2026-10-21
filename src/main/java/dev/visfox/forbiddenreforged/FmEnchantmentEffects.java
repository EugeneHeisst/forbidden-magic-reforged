package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class FmEnchantmentEffects {
    private static final Set<Item> GARBAGE = Set.of(Items.DIRT, Items.SAND, Items.GRAVEL, Items.COBBLESTONE, Items.NETHERRACK);
    private static final TagKey<Block> INFUSED_STONES = TagKey.create(Registries.BLOCK, ForbiddenMagic.id("infused_stones"));
    private static final List<String> SIN_POOL = List.of("wrath", "envy", "gluttony", "pride", "lust", "sloth", "greed");

    private FmEnchantmentEffects() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onBlockDrops);
    }

    public static boolean isGarbage(ItemStack stack) {
        return GARBAGE.contains(stack.getItem());
    }

    public static ItemStack sinShard(RandomSource random) {
        while (true) {
            String vice = SIN_POOL.get(random.nextInt(SIN_POOL.size()));
            if (vice.equals("lust") && FmConfig.lustDisabled()) {
                continue;
            }
            if (vice.equals("gluttony")) {
                return new ItemStack(FmItems.GLUTTONY_SHARD.get());
            }
            return new ItemStack(FmItems.DEADLY_SHARDS.get(vice).get());
        }
    }

    public static boolean corruptRoll(RandomSource random) {
        return random.nextInt(3) == 1;
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        if (!FmEnchantments.enabled()) {
            return;
        }
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof ServerPlayer player) || breaker instanceof FakePlayer) {
            return;
        }
        ItemStack tool = event.getTool();
        int consuming = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CONSUMING);
        if (consuming > 0) {
            event.getDrops().removeIf(drop -> isGarbage(drop.getItem()));
        }
        int corrupting = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CORRUPTING);
        if (corrupting > 0 && event.getState().is(INFUSED_STONES)) {
            RandomSource random = event.getLevel().getRandom();
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (corruptRoll(random) && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("thaumcraft")) {
                    drop.setItem(sinShard(random).copyWithCount(stack.getCount()));
                }
            }
        }
    }
}

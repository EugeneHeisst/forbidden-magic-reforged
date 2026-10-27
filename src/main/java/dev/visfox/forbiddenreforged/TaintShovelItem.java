package dev.visfox.forbiddenreforged;

import dev.eugheist.tc4modern.TaintBlock;
import dev.eugheist.tc4modern.TaintFibresBlock;
import dev.visfox.forbiddenreforged.compat.item.gear.TcMaterials;
import dev.visfox.forbiddenreforged.compat.registry.ModBlocks;
import dev.visfox.forbiddenreforged.compat.registry.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintShovelItem extends Item {
    public TaintShovelItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isTaintMaterial(state)) {
            return TcMaterials.ELEMENTAL_TOOL.miningSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        int purified = 0;

        for (int x = center.getX() - 5; x <= center.getX() + 5; x++) {
            for (int y = center.getY() - 4; y <= center.getY() + 4; y++) {
                for (int z = center.getZ() - 5; z <= center.getZ() + 5; z++) {
                    BlockPos target = new BlockPos(x, y, z);
                    if (!level.hasChunkAt(target)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (!state.is(ModBlocks.FLUX_GOO.get())
                            && !state.is(ModBlocks.FLUX_GAS.get())) {
                        continue;
                    }
                    purified++;
                    if (!level.isClientSide()) {
                        level.removeBlock(target, false);
                    }
                }
            }
        }

        if (purified == 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player != null) {
            stack.hurtAndBreak(
                    Math.min(purified, 15),
                    player,
                    context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                            ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                            : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            level.playSound(
                    null,
                    center,
                    ModSounds.WANDFAIL.get(),
                    SoundSource.PLAYERS,
                    0.2F,
                    0.2F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isTaintMaterial(BlockState state) {
        return state.getBlock() instanceof TaintBlock
                || state.getBlock() instanceof TaintFibresBlock
                || state.is(ModBlocks.FLUX_GOO.get())
                || state.is(ModBlocks.FLUX_GAS.get())
                || state.is(FmBlocks.TAINT_LEAVES.get());
    }
}

package dev.visfox.forbiddenreforged;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.item.gear.WarpingGear;
import thaumcraft.registry.ModSounds;

import java.util.ArrayList;
import java.util.List;

public class MorphToolItem extends Item implements WarpingGear {
    public MorphToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getMaxDamage() - stack.getDamageValue() <= 5) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            MorphState state = stack.getOrDefault(FmComponents.MORPH_STATE.get(), MorphState.EMPTY);
            String current = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName().getString() : "";
            MorphState next = state.store(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY), current).advance();
            stack.set(FmComponents.MORPH_STATE.get(), next);
            stack.set(DataComponents.ENCHANTMENTS, next.currentSet());
            if (next.currentName().isEmpty()) {
                stack.remove(DataComponents.CUSTOM_NAME);
            } else {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(next.currentName()).withStyle(ChatFormatting.ITALIC));
            }
            stack.hurtAndBreak(5, player, EquipmentSlot.MAINHAND);
            level.playSound(null, player.blockPosition(), ModSounds.WANDFAIL.get(), SoundSource.PLAYERS, 0.2F, 0.2F + player.getRandom().nextFloat() * 0.2F);
        }
        player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (!(owner instanceof LivingEntity) || !stack.isDamaged() || owner.tickCount % 10 != 0) {
            return;
        }
        if (FmEnchantments.level(level.registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return FmEnchantments.level(player.level().registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0 ? 1 : 0;
    }

    public static List<BlockPos> impactArea(Direction facing, BlockPos origin) {
        Direction.Axis axis = facing.getAxis();
        List<BlockPos> area = new ArrayList<>(9);
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                area.add(switch (axis) {
                    case Y -> origin.offset(first, 0, second);
                    case X -> origin.offset(0, first, second);
                    case Z -> origin.offset(first, second, 0);
                });
            }
        }
        return area;
    }

    public static Direction dominantDirection(Vec3 look) {
        double x = Math.abs(look.x);
        double y = Math.abs(look.y);
        double z = Math.abs(look.z);
        if (y >= x && y >= z) {
            return look.y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (x >= z) {
            return look.x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return look.z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (!level.isClientSide()
            && FmEnchantments.level(level.registryAccess(), stack, FmEnchantments.IMPACT) > 0
            && stack.isCorrectToolForDrops(state)) {
            Direction facing = dominantDirection(owner.getLookAngle());
            for (BlockPos target : impactArea(facing, pos)) {
                if (target.equals(pos)) {
                    continue;
                }
                BlockState neighbour = level.getBlockState(target);
                if (neighbour.isAir() || !stack.isCorrectToolForDrops(neighbour)
                    || neighbour.getDestroySpeed(level, target) < 0.0F) {
                    continue;
                }
                stack.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
                level.destroyBlock(target, true, owner, 512);
            }
        }
        return super.mineBlock(stack, level, state, pos, owner);
    }
}

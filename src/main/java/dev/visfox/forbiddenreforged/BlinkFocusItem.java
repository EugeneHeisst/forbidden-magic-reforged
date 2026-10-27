package dev.visfox.forbiddenreforged;

import dev.eugheist.tc4modern.FociContent;
import dev.visfox.forbiddenreforged.compat.wand.Wands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.util.List;

import javax.annotation.Nullable;

public class BlinkFocusItem extends FociContent.Focus {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.ENTROPY, 300);
    private static final AspectList COST_HELLFIRE =
            new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.FIRE, 100);
    private static final AspectList COST_PANDEMONIUM =
            new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.ORDER, 100);

    public static final int HELLFIRE = 100;
    public static final int PANDEMONIUM = 101;

    public BlinkFocusItem(Properties properties) {
        super();
    }

    @Override
    public String getSortingHelperBase() {
        return "BL";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x909090;
    }

    @Override
    public String getDepthTexture(ItemStack focus) {
        return "fm_blink";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, HELLFIRE)
                ? COST_HELLFIRE
                : this.isUpgradedWith(focus, PANDEMONIUM) ? COST_PANDEMONIUM : COST_BASE;
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return 500;
    }

    @Override
    public @Nullable int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        switch (rank) {
            case 1:
                return new int[] {1};
            case 2:
                return new int[] {HELLFIRE, PANDEMONIUM, 1};
            case 3:
            case 4:
            case 5:
                if (this.isUpgradedWith(focus, HELLFIRE)) {
                    return new int[] {0, 1, 3};
                } else if (this.isUpgradedWith(focus, PANDEMONIUM)) {
                    return new int[] {1, 3};
                } else {
                    return new int[] {1};
                }
            default:
                return null;
        }
    }

    @Override
    public boolean onFocusRightClick(ItemStack wand, Level level, Player player) {
        HitResult hit = player.pick(32, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)
                || blockHit.getType() == HitResult.Type.MISS) {
            return false;
        }
        if (!FociContent.consumeAllVis(
                wand, player, this.getVisCost(Wands.getFocusItem(wand)), !level.isClientSide())) {
            return false;
        }
        if (level.isClientSide()) {
            return true;
        }
        Vec3 targetPos =
                Vec3.atBottomCenterOf(blockHit.getBlockPos().relative(blockHit.getDirection()));
        double range = 3.0 + 1.5 * Wands.getFocusEnlarge(wand);
        ItemStack focus = Wands.getFocusItem(wand);
        if (this.isUpgradedWith(focus, HELLFIRE)) {
            AABB aabb =
                    new AABB(
                            targetPos.x - range,
                            targetPos.y - range,
                            targetPos.z - range,
                            targetPos.x + range,
                            targetPos.y + range,
                            targetPos.z + range);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
            int potency = Wands.getFocusPotency(wand);
            float damage = 3.0F + 3.0F * potency;
            for (LivingEntity entity : entities) {
                if (entity == player) {
                    continue;
                }
                entity.hurt(level.damageSources().inFire(), damage);
                entity.setRemainingFireTicks((3 + 3 * potency) * 20);
            }
        } else if (this.isUpgradedWith(focus, PANDEMONIUM)) {
            AABB aabb =
                    new AABB(
                            targetPos.x - range,
                            targetPos.y - range,
                            targetPos.z - range,
                            targetPos.x + range,
                            targetPos.y + range,
                            targetPos.z + range);
            List<LivingEntity> enemies =
                    level.getEntitiesOfClass(
                            LivingEntity.class, aabb, entity -> entity instanceof Enemy);
            for (LivingEntity enemy : enemies) {
                enemy.teleportTo(player.getX(), player.getY(), player.getZ());
            }
        }
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }
        return true;
    }
}

package dev.visfox.forbiddenreforged.compat.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ToolHarvest {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    public static boolean isHarvesting() {
        return ACTIVE.get();
    }

    public static void breakArea(
            ItemStack stack,
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            java.util.function.Predicate<BlockState> accepts,
            int ignored) {
        if (ACTIVE.get() || player.isShiftKeyDown()) return;
        ACTIVE.set(true);
        try {
            var look = player.getLookAngle();
            var axis =
                    Math.abs(look.y) > .6
                            ? net.minecraft.core.Direction.Axis.Y
                            : Math.abs(look.x) > Math.abs(look.z)
                                    ? net.minecraft.core.Direction.Axis.X
                                    : net.minecraft.core.Direction.Axis.Z;
            for (int a = -1; a <= 1; a++)
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) continue;
                    var pos =
                            switch (axis) {
                                case X -> origin.offset(0, a, b);
                                case Y -> origin.offset(a, 0, b);
                                case Z -> origin.offset(a, b, 0);
                            };
                    var state = level.getBlockState(pos);
                    if (!state.isAir()
                            && state.getDestroySpeed(level, pos) >= 0
                            && accepts.test(state)
                            && level.mayInteract(player, pos)
                            && !player.blockActionRestricted(
                                    level, pos, player.gameMode.getGameModeForPlayer()))
                        player.gameMode.destroyBlock(pos);
                }
        } finally {
            ACTIVE.set(false);
        }
    }
}

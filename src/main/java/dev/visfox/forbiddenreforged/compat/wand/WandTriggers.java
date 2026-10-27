package dev.visfox.forbiddenreforged.compat.wand;

import net.minecraft.core.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;

public final class WandTriggers {
    public interface Trigger {
        boolean apply(Level l, ItemStack w, Player p, BlockPos pos, Direction face);
    }

    public static void register(java.util.function.Predicate<BlockState> blocks, Trigger trigger) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock
                                e) -> {
                    if (blocks.test(e.getLevel().getBlockState(e.getPos()))
                            && trigger.apply(
                                    e.getLevel(),
                                    e.getItemStack(),
                                    e.getEntity(),
                                    e.getPos(),
                                    e.getFace())) {
                        e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                        e.setCanceled(true);
                    }
                });
    }
}

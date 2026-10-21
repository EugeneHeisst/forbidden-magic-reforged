package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class WrathCageBlock extends Block implements EntityBlock {
    public WrathCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WrathCageBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != FmBlockEntities.WRATH_CAGE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> WrathCageBlockEntity.tick(tickLevel, pos, tickState, (WrathCageBlockEntity) entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage) || level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getMainHandItem();
        String incoming = MobCrystalItem.mob(held);
        if (incoming == null || !FmSpawnerMobs.ids().contains(incoming)) {
            return InteractionResult.PASS;
        }
        String previous = cage.getMobId();
        cage.setMobId(incoming);
        if (previous == null) {
            held.remove(FmComponents.MOB_ID.get());
        } else {
            MobCrystalItem.setMob(held, previous);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (blockEntity instanceof WrathCageBlockEntity cage && cage.getMobId() != null) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                WrathCageBlockEntity.crystalForBreak(cage));
            level.addFreshEntity(drop);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}

package dev.visfox.forbiddenreforged.compat.blockentity;

import net.minecraft.core.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TcBlockEntity extends BlockEntity {
    protected TcBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider r) {
        return saveWithoutMetadata(r);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public void preRemoveSideEffects(BlockPos pos, BlockState state) {}

    public static void beforeRemove(BlockState state, Level level, BlockPos pos, BlockState next) {
        if (!state.is(next.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof TcBlockEntity tile)
            tile.preRemoveSideEffects(pos, state);
    }
}

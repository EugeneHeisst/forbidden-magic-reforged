package dev.visfox.forbiddenreforged;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<TaintSaplingBlock> CODEC = simpleCodec(TaintSaplingBlock::new);
    public TaintSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends TaintSaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0 && level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            grow(level, pos, random);
        }
    }

    private void grow(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!TaintTree.grow(level, pos, random)) {
            level.setBlockAndUpdate(pos, defaultBlockState());
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos, random);
    }
}

package dev.visfox.forbiddenreforged.compat.taint;

public final class TaintBiomes {
    public static boolean isTainted(
            net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level.getBiome(pos).is(dev.eugheist.tc4modern.TaintBiomes.TAINTED_LAND);
    }
}

package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(targets = "dev.eugheist.ttmodern.EnchanterMenu$1", remap = false)
public abstract class TinkererDataSizeMixin {
    @ModifyConstant(method = "*", constant = @Constant(intValue = 40), require = 1)
    private int size40(int original) {
        return dev.visfox.forbiddenreforged.FmTinkerer.totalEntries;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 41), require = 1)
    private int size41(int original) {
        return dev.visfox.forbiddenreforged.FmTinkerer.totalEntries + 1;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 47), require = 1)
    private int size47(int original) {
        return dev.visfox.forbiddenreforged.FmTinkerer.totalEntries + 7;
    }
}

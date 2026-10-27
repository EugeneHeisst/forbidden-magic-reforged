package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.ResearchWarp.class, remap = false)
public abstract class ResearchWarpMixin {
    @Inject(method = "amount", at = @At("RETURN"), cancellable = true)
    private static void addon(String key, CallbackInfoReturnable<Integer> cir) {
        var n = dev.visfox.forbiddenreforged.compat.research.Warp.RESEARCH.get(key);
        if (n != null) cir.setReturnValue(n);
    }
}

package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(targets = "dev.eugheist.ttmodern.EnchanterCosts", remap = false)
public abstract class TinkererCostsMixin {
    @Shadow @Final @Mutable private static Map<String, Object> ENTRIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addon(CallbackInfo ci) {
        ENTRIES = dev.visfox.forbiddenreforged.FmTinkerer.addCosts(ENTRIES);
    }
}

package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.GogglesAspectTags.class, remap = false)
public abstract class GogglesContainerMixin {
    @Inject(method = "aspects", at = @At("HEAD"), cancellable = true)
    private static void cage(
            net.minecraft.world.level.block.entity.BlockEntity tile,
            CallbackInfoReturnable<thaumcraft.api.aspects.AspectList> cir) {
        if (tile instanceof dev.visfox.forbiddenreforged.WrathCageBlockEntity cage)
            cir.setReturnValue(cage.getAspects());
    }
}

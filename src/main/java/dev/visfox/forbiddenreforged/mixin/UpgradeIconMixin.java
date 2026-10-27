package dev.visfox.forbiddenreforged.mixin;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.FociContent.UpgradeType.class, remap = false)
public abstract class UpgradeIconMixin {
    @Shadow
    public abstract int id();

    @Inject(method = "icon", at = @At("HEAD"), cancellable = true)
    private void addon(CallbackInfoReturnable<ResourceLocation> cir) {
        if (id() == 100 || id() == 101)
            cir.setReturnValue(
                    dev.visfox.forbiddenreforged.ForbiddenMagic.id(
                            "textures/misc/"
                                    + (id() == 100 ? "hellfire" : "pandemonium")
                                    + ".png"));
    }
}

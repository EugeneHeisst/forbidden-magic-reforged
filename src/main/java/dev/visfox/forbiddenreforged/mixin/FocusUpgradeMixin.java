package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.FociContent.class, remap = false)
public abstract class FocusUpgradeMixin {
    @Inject(method = "upgradeType", at = @At("HEAD"), cancellable = true)
    private static void addon(
            int id, CallbackInfoReturnable<dev.eugheist.tc4modern.FociContent.UpgradeType> cir) {
        if (id == 100)
            cir.setReturnValue(
                    new dev.eugheist.tc4modern.FociContent.UpgradeType(
                            id,
                            "hellfire",
                            new thaumcraft.api.aspects.AspectList()
                                    .add(dev.visfox.forbiddenreforged.FmAspects.NETHER, 1)));
        if (id == 101)
            cir.setReturnValue(
                    new dev.eugheist.tc4modern.FociContent.UpgradeType(
                            id,
                            "pandemonium",
                            new thaumcraft.api.aspects.AspectList()
                                    .add(thaumcraft.api.aspects.Aspect.DARKNESS, 1)));
    }
}

package dev.visfox.forbiddenreforged.mixin;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.AlchemyContent.class, remap = false)
public abstract class DepthTextureMixin {
    @Inject(method = "id", at = @At("HEAD"), cancellable = true)
    private static void addon(String path, CallbackInfoReturnable<ResourceLocation> cir) {
        if (path.equals("textures/items/fm_blink.png"))
            cir.setReturnValue(
                    dev.visfox.forbiddenreforged.ForbiddenMagic.id(
                            "textures/item/blink_focus.png"));
    }
}

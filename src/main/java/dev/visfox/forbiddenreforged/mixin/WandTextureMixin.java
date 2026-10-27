package dev.visfox.forbiddenreforged.mixin;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.WandRenderer.class, remap = false)
public abstract class WandTextureMixin {
    @Inject(method = "tex", at = @At("HEAD"), cancellable = true)
    private static void addon(String path, CallbackInfoReturnable<ResourceLocation> cir) {
        String key =
                path.startsWith("wand_rod_")
                        ? "rod:" + path.substring(9)
                        : path.startsWith("wand_cap_") ? "cap:" + path.substring(9) : "";
        var texture = dev.visfox.forbiddenreforged.compat.wand.WandRegistry.TEXTURES.get(key);
        if (texture != null) cir.setReturnValue(texture);
    }
}

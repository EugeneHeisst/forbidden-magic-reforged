package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = thaumcraft.common.tiles.infusion.InfusionEnchantments.class, remap = false)
public abstract class InfusionEnchantmentsMixin {
    @Inject(method = "all", at = @At("RETURN"), cancellable = true)
    private static void addon(
            CallbackInfoReturnable<
                            List<thaumcraft.common.tiles.infusion.InfusionEnchantments.Recipe>>
                    cir) {
        var out = new ArrayList<>(cir.getReturnValue());
        out.addAll(dev.visfox.forbiddenreforged.compat.crafting.TcRecipes.ENCHANTMENTS);
        cir.setReturnValue(List.copyOf(out));
    }
}

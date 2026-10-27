package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.eugheist.tc4modern.WarpGear.class, remap = false)
public abstract class WarpGearMixin {
    @Inject(method = "contribution", at = @At("RETURN"), cancellable = true)
    private static void addon(
            net.minecraft.world.item.ItemStack stack,
            net.minecraft.world.entity.player.Player player,
            CallbackInfoReturnable<Integer> cir) {
        var id =
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem())
                        .toString();
        var n = dev.visfox.forbiddenreforged.compat.research.Warp.ITEMS.get(id);
        if (n != null) cir.setReturnValue(cir.getReturnValue() + n);
    }
}

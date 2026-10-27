package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = dev.eugheist.tc4modern.Accessories.class, remap = false)
public interface AccessoriesAccessor {
    @Invoker("visAmuletTick")
    static void tick(
            net.minecraft.world.item.ItemStack stack,
            net.minecraft.world.entity.player.Player player) {
        throw new AssertionError();
    }
}

package dev.visfox.forbiddenreforged.compat.wand;

import dev.eugheist.tc4modern.*;

import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

public final class WandKeys {
    public static final WandKeys PARTS = new WandKeys();

    public void set(ItemStack stack, WandParts parts) {
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                stack,
                t -> {
                    t.putString("cap", parts.cap());
                    t.putString("rod", parts.rod());
                    t.putBoolean("sceptre", parts.sceptre());
                });
    }
}

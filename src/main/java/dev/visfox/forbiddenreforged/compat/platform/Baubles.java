package dev.visfox.forbiddenreforged.compat.platform;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Baubles {
    public static ItemStack get(Player p, int slot) {
        return dev.eugheist.tc4modern.BaublesInventory.of(p).getItem(slot);
    }

    public static void set(Player p, int slot, ItemStack stack) {
        dev.eugheist.tc4modern.BaublesInventory.of(p).setItem(slot, stack);
    }
}

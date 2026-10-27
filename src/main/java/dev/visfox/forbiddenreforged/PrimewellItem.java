package dev.visfox.forbiddenreforged;

import dev.eugheist.tc4modern.ScribingToolsItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Original Forbidden Magic Primewell: an epic scribing tool whose charge is never consumed. The
 * table API checks ScribingToolsItem and wears stacks directly, so an effectively unbounded max
 * damage plus a hidden bar preserves the original permanent-use behavior without requiring a host
 * patch.
 */
public final class PrimewellItem extends ScribingToolsItem {
    public PrimewellItem(Properties properties) {
        super(properties.durability(Integer.MAX_VALUE).rarity(Rarity.EPIC));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }
}

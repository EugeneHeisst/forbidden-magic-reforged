package dev.visfox.forbiddenreforged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DiabolistForkItem extends Item {
    public DiabolistForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public Component getName(ItemStack stack) {
        return FmConfig.spork()
                ? Component.translatable("item.forbiddenmagic.diabolist_fork_spork")
                : super.getName(stack);
    }
}

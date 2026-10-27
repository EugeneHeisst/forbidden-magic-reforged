package dev.visfox.forbiddenreforged.compat.wand;

import dev.eugheist.tc4modern.*;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

public interface RodUpdate {
    void onUpdate(ItemStack stack, Player player);
}

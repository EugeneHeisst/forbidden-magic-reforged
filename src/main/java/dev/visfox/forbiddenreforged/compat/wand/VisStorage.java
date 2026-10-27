package dev.visfox.forbiddenreforged.compat.wand;

import dev.eugheist.tc4modern.*;

import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

public final class VisStorage {
    public static int addRealVis(ItemStack s, Aspect a, int n, boolean apply) {
        return dev.visfox.forbiddenreforged.compat.item.bauble.VisAmuletItem.add(s, a, n, apply);
    }
}

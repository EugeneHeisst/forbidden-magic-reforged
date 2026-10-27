package dev.visfox.forbiddenreforged.compat.wand;

import dev.eugheist.tc4modern.*;

import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

public final class Wands {
    public static boolean isWand(ItemStack s) {
        return WandData.isWand(s);
    }

    public static ItemStack create(WandCap c, WandRod r, boolean sceptre) {
        var cap = dev.eugheist.tc4modern.WandParts.CAPS.get(c.tag());
        var rod = dev.eugheist.tc4modern.WandParts.RODDEFS.get(r.tag());
        return sceptre
                ? dev.eugheist.tc4modern.WandParts.sceptre(cap, rod)
                : dev.eugheist.tc4modern.WandParts.make(cap, rod);
    }

    public static WandRod getRod(ItemStack s) {
        return new WandRod(dev.eugheist.tc4modern.WandParts.rod(s).tag());
    }

    public static WandCap getCap(ItemStack s) {
        return new WandCap(dev.eugheist.tc4modern.WandParts.cap(s).tag());
    }

    public static WandParts getParts(ItemStack s) {
        return new WandParts(
                getCap(s).tag(), getRod(s).tag(), dev.eugheist.tc4modern.WandParts.isSceptre(s));
    }

    public static int getMaxVis(ItemStack s) {
        return WandData.capacity(s);
    }

    public static int getVis(ItemStack s, Aspect a) {
        return WandData.get(s, a);
    }

    public static int addVis(ItemStack s, Aspect a, int n, boolean apply) {
        return WandData.add(s, a, n, apply);
    }

    public static int addRealVis(ItemStack s, Aspect a, int n, boolean apply) {
        var tag =
                s.getOrDefault(
                                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                net.minecraft.world.item.component.CustomData.EMPTY)
                        .copyTag();
        int left =
                thaumcraft.common.lib.vis.VisStorage.addRealVis(
                        tag, a, n, apply, WandData.capacity(s));
        if (apply)
            s.set(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(tag));
        return left;
    }

    public static void fill(ItemStack s) {
        for (var a : Aspect.getPrimalAspects()) WandData.add(s, a, WandData.capacity(s), true);
    }

    public static FociContent.Focus getFocus(ItemStack s) {
        return FociContent.getFocus(s);
    }

    public static ItemStack getFocusItem(ItemStack s) {
        return FociContent.getFocusItem(s);
    }

    public static int getFocusPotency(ItemStack s) {
        return FociContent.getFocusPotency(s);
    }

    public static int getFocusEnlarge(ItemStack s) {
        var f = getFocus(s);
        return f == null ? 0 : f.getUpgradeLevel(getFocusItem(s), 3);
    }

    public static int getFocusTreasure(ItemStack s) {
        var f = getFocus(s);
        return f == null ? 0 : f.getUpgradeLevel(getFocusItem(s), 2);
    }
}

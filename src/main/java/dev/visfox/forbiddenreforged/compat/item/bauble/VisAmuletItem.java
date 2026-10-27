package dev.visfox.forbiddenreforged.compat.item.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.Aspect;

public class VisAmuletItem extends Item implements baubles.api.IBauble {
    public VisAmuletItem(Properties p, boolean upgraded) {
        super(p.stacksTo(1));
    }

    public baubles.api.BaubleType getBaubleType(ItemStack s) {
        return baubles.api.BaubleType.AMULET;
    }

    public static int add(ItemStack s, Aspect a, int n, boolean apply) {
        var data =
                s.getOrDefault(
                                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                net.minecraft.world.item.component.CustomData.EMPTY)
                        .copyTag();
        int before = data.getInt(a.getTag()), accepted = Math.min(n, 2500 - before);
        if (apply) {
            data.putInt(a.getTag(), before + accepted);
            s.set(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(data));
        }
        return n - accepted;
    }

    @Override
    public void onWornTick(ItemStack s, LivingEntity entity) {
        if (entity instanceof Player p)
            dev.visfox.forbiddenreforged.mixin.AccessoriesAccessor.tick(s, p);
    }
}

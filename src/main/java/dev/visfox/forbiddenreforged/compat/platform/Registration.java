package dev.visfox.forbiddenreforged.compat.platform;

import dev.visfox.forbiddenreforged.compat.item.gear.TcMaterials.Material;

import net.minecraft.world.item.*;

public final class Registration {
    public static Item.Properties food(Item.Properties p, int nutrition, float saturation) {
        return p.food(
                new net.minecraft.world.food.FoodProperties.Builder()
                        .nutrition(nutrition)
                        .saturationModifier(saturation)
                        .build());
    }

    private static Item.Properties tool(
            Item.Properties p,
            Material m,
            net.minecraft.world.item.component.Tool tool,
            float damage,
            float speed) {
        return p.durability(m.tier().getUses())
                .attributes(SwordItem.createAttributes(m.tier(), damage, speed))
                .component(net.minecraft.core.component.DataComponents.TOOL, tool);
    }

    public static Item.Properties sword(Item.Properties p, Material m, float d, float s) {
        return tool(p, m, SwordItem.createToolProperties(), d, s);
    }

    public static Item.Properties pickaxe(Item.Properties p, Material m, float d, float s) {
        return tool(
                p,
                m,
                m.tier().createToolProperties(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE),
                d,
                s);
    }

    public static Item.Properties axe(Item.Properties p, Material m, float d, float s) {
        return tool(
                p,
                m,
                m.tier().createToolProperties(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE),
                d,
                s);
    }

    public static Item.Properties shovel(Item.Properties p, Material m, float d, float s) {
        return tool(
                p,
                m,
                m.tier().createToolProperties(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL),
                d,
                s);
    }
}

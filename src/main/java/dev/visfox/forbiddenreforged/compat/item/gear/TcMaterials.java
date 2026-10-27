package dev.visfox.forbiddenreforged.compat.item.gear;

import net.minecraft.world.item.Tier;

public final class TcMaterials {
    public record Material(Tier tier) {
        public float miningSpeed() {
            return tier.getSpeed();
        }
    }

    public static final Material
            ELEMENTAL_TOOL = new Material(dev.eugheist.tc4modern.ElementalTools.TIER),
            THAUMIUM_TOOL = new Material(dev.eugheist.tc4modern.ThaumiumContent.TIER);
}

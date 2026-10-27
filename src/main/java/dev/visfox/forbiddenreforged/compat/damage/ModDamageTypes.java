package dev.visfox.forbiddenreforged.compat.damage;

public final class ModDamageTypes {
    public static final net.minecraft.resources.ResourceKey<
                    net.minecraft.world.damagesource.DamageType>
            TAINT =
                    net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                            net.minecraft.resources.ResourceLocation.parse("tc4modern:taint"));
}

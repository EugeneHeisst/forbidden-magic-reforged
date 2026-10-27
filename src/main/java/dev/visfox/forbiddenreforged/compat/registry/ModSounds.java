package dev.visfox.forbiddenreforged.compat.registry;

public final class ModSounds {
    public static final java.util.function.Supplier<net.minecraft.sounds.SoundEvent> WANDFAIL =
            () ->
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(
                            net.minecraft.resources.ResourceLocation.parse("tc4modern:wandfail"));
}

package dev.visfox.forbiddenreforged;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FmConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static ModConfigSpec.BooleanValue noLust;
    public static ModConfigSpec.IntValue gluttony;
    public static ModConfigSpec.BooleanValue enchanting;

    static {
        noLust = BUILDER.comment("Enable to remove Luxuria aspect and related items.").define("general.no_lust", false);
        gluttony = BUILDER.comment("0 = Enabled, 1 = Disabled, 2 = Hardcore").defineInRange("general.gluttony", 0, 0, 2);
        enchanting = BUILDER.comment("Enable Forbidden Magic enchantments.").define("general.enchanting", true);
        SPEC = BUILDER.build();
    }

    private FmConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, SPEC);
    }

    public static boolean lustDisabled() {
        return SPEC.isLoaded() ? noLust.get() : noLust.getDefault();
    }

    public static boolean enchantmentsEnabled() {
        return SPEC.isLoaded() ? enchanting.get() : enchanting.getDefault();
    }
}

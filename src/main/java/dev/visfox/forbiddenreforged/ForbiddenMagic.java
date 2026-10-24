package dev.visfox.forbiddenreforged;

import com.mojang.logging.LogUtils;
import dev.visfox.forbiddenreforged.test.FmGameTests;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ForbiddenMagic.MODID)
public final class ForbiddenMagic {
    public static final String MODID = "forbiddenmagic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmAspects.init();
        FmGameTests.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

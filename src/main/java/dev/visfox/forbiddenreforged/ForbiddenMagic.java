package dev.visfox.forbiddenreforged;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ForbiddenMagic.MODID)
public final class ForbiddenMagic {
    public static final String MODID = "forbiddenmagic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmConfig.register(modContainer);
        FmBlocks.register(modEventBus);
        FmItems.ITEMS.register(modEventBus);
        FmCreativeTab.register(modEventBus);
        FmAspectTags.register();
        NeoForge.EVENT_BUS.addListener(FmItemEvents::onFuel);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

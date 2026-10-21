package fox.spiteful.forbidden;

import fox.spiteful.forbidden.test.FmGameTests;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ForbiddenMagic.MODID)
public final class ForbiddenMagic {
    public static final String MODID = "forbiddenmagic";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmGameTests.register(modEventBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}

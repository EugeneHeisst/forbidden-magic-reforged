package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.compat.aspect.AddonAspects;
import dev.visfox.forbiddenreforged.compat.research.Warp;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {}

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(
                () -> {
                    AddonAspects.init();
                    FmAspectTags.register();
                    logAspectRegistry();
                    dev.eugheist.tc4modern.api.AspectContainers.register(
                            tile ->
                                    tile instanceof WrathCageBlockEntity cage
                                            ? new dev.eugheist.tc4modern.api.AspectContainers
                                                    .Container() {
                                                public thaumcraft.api.aspects.AspectList
                                                        getAspects() {
                                                    return cage.getAspects();
                                                }

                                                public int capacity() {
                                                    return 64;
                                                }

                                                public boolean canExtract() {
                                                    return false;
                                                }

                                                public boolean canInject() {
                                                    return true;
                                                }

                                                public boolean accepts(
                                                        thaumcraft.api.aspects.Aspect aspect) {
                                                    return cage.doesContainerAccept(aspect);
                                                }

                                                public boolean take(
                                                        thaumcraft.api.aspects.Aspect aspect,
                                                        int amount) {
                                                    return false;
                                                }

                                                public int add(
                                                        thaumcraft.api.aspects.Aspect aspect,
                                                        int amount) {
                                                    return cage.addToContainer(aspect, amount);
                                                }
                                            }
                                            : null);
                    FmWands.register();
                    FmWandTriggers.register();
                    FmRecipes.register();
                    thaumcraft.common.config.ConfigResearchData.init();
                    FmResearch.register();
                    Warp.registerItem(ForbiddenMagic.id("wrath_cage"), 5);
                    Warp.registerItem(ForbiddenMagic.id("taint_sapling"), 1);
                    FmTinkerer.register();
                });
    }

    private static void logAspectRegistry() {
        int fmAddonCount = FmAspectDiagnostics.addonLines().size();
        int expectedAddonCount = FmConfig.lustDisabled() ? 6 : 7;
        String luxuriaStatus;
        if (FmConfig.lustDisabled()) {
            luxuriaStatus = "disabled-by-config";
        } else if (FmAspects.LUST == null) {
            luxuriaStatus = "missing-registration-error";
        } else {
            luxuriaStatus = "registered";
        }
        ForbiddenMagic.LOGGER.info(
                "Aspect registry summary: total={} builtin={} registeredAddon={} fmAddon={}"
                        + " expectedFmAddon={} luxuria={}",
                FmAspects.count(),
                FmAspects.BUILTIN_ASPECT_COUNT,
                FmAspects.count() - FmAspects.BUILTIN_ASPECT_COUNT,
                fmAddonCount,
                expectedAddonCount,
                luxuriaStatus);
        for (String line : FmAspectDiagnostics.registryLines()) {
            ForbiddenMagic.LOGGER.info("Aspect registry: {}", line);
        }
    }
}

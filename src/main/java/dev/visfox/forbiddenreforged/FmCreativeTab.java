package dev.visfox.forbiddenreforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import thaumcraft.registry.ModCreativeTabs;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmCreativeTab {
    private FmCreativeTab() {
    }

    public static boolean visible(String vice) {
        return !vice.equals("lust") || !FmConfig.lustDisabled();
    }

    @SubscribeEvent
    static void addItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != ModCreativeTabs.THAUMCRAFT.get()) {
            return;
        }
        for (var entry : FmItems.DEADLY_SHARDS.entrySet()) {
            if (visible(entry.getKey())) {
                event.accept(entry.getValue().get());
            }
        }
        event.accept(FmItems.GLUTTONY_SHARD.get());
        event.accept(FmItems.SKULL_AXE.get());
        event.accept(FmItems.EMERALD_FRAGMENT.get());
        event.accept(FmItems.MORPH_PICKAXE.get());
        event.accept(FmItems.MORPH_SWORD.get());
        event.accept(FmItems.MORPH_SHOVEL.get());
        event.accept(FmItems.MORPH_AXE.get());
    }
}

package dev.visfox.forbiddenreforged;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForbiddenMagic.MODID);

    public static final Supplier<CreativeModeTab> FORBIDDEN = TABS.register("forbidden", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.forbiddenmagic"))
            .icon(() -> new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get()))
            .displayItems((parameters, output) -> {
                for (var entry : FmItems.ITEMS.getEntries()) {
                    output.accept(entry.get());
                }
            })
            .build()
    );

    private FmCreativeTab() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}

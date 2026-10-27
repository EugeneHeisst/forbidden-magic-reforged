package dev.visfox.forbiddenreforged;

import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class FmItemEvents {
    private FmItemEvents() {}

    public static void onFuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(FmItems.TAINT_COAL.get())) {
            // The original taint_coal_burn provider is uniform in [1, 2400].
            event.setBurnTime(ThreadLocalRandom.current().nextInt(1, 2401));
        }
    }
}

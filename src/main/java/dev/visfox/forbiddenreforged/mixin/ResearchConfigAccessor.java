package dev.visfox.forbiddenreforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "dev.eugheist.tc4modern.ResearchConfig", remap = false)
public interface ResearchConfigAccessor {
    @Invoker("difficulty")
    static int difficulty() {
        throw new AssertionError();
    }
}

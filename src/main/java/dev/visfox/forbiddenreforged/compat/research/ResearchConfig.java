package dev.visfox.forbiddenreforged.compat.research;

public final class ResearchConfig {
    public static int difficulty() {
        return dev.visfox.forbiddenreforged.mixin.ResearchConfigAccessor.difficulty();
    }
}

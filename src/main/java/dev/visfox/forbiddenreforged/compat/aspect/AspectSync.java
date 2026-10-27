package dev.visfox.forbiddenreforged.compat.aspect;

public final class AspectSync {
    public static void addRegistrar(java.util.function.Consumer<AspectRegistry> registrar) {
        registrar.accept(new AspectRegistry());
    }
}

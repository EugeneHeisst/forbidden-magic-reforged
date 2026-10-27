package dev.visfox.forbiddenreforged.compat.aspect;

import thaumcraft.api.aspects.AspectList;

public class AspectRegistrar {
    private final AspectRegistry registry;

    protected AspectRegistrar(AspectRegistry registry) {
        this.registry = registry;
    }

    protected AspectList tags() {
        return new AspectList();
    }

    protected void register(String id, AspectList tags) {
        registry.register(id, tags);
    }
}

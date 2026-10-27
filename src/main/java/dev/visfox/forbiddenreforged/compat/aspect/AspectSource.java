package dev.visfox.forbiddenreforged.compat.aspect;

import thaumcraft.api.aspects.*;

public interface AspectSource extends dev.eugheist.tc4modern.api.AspectSource {
    AspectList getAspects();

    void setAspects(AspectList a);

    boolean doesContainerAccept(Aspect a);

    int addToContainer(Aspect a, int n);

    boolean doesContainerContainAmount(Aspect a, int n);

    int containerContains(Aspect a);
}

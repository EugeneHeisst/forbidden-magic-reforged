package dev.visfox.forbiddenreforged;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

public final class FmResearch {
    private FmResearch() {
    }

    public static void register() {
        ResearchCategories.registerCategory(
            "FORBIDDEN",
            ForbiddenMagic.id("textures/misc/forbidden.png"),
            ForbiddenMagic.id("textures/misc/runecircle.png")
        );
        new ResearchItem("NETHERSHARDS", "FORBIDDEN", new AspectList(), -8, -5, 0, TcStack.of(FmItems.id("wrath").toString(), 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.1"),
                ResearchPage.text(FmConfig.lustDisabled() ? "forbiddenmagic.research_page.NETHERSHARDS.2b" : "forbiddenmagic.research_page.NETHERSHARDS.2"),
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.3")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem(
            "HELLFIRE",
            "FORBIDDEN",
            new AspectList().add(Aspect.FIRE, 3).add(FmAspects.NETHER, 4).add(Aspect.TRAVEL, 2),
            -8,
            -8,
            1,
            TcStack.of("minecraft:flint_and_steel", 1)
        )
            .setPages(ResearchPage.text("forbiddenmagic.research_page.HELLFIRE.1"))
            .setRound()
            .setHidden()
            .setParents("NETHERSHARDS")
            .setAspectTriggers(FmAspects.NETHER)
            .registerResearchItem();
        new ResearchItem(
            "SKULLAXE",
            "FORBIDDEN",
            new AspectList().add(Aspect.WEAPON, 3).add(FmAspects.WRATH, 4).add(FmAspects.NETHER, 1),
            -11,
            -7,
            2,
            TcStack.of(FmItems.SKULL_AXE.getId().toString(), 1)
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.SKULLAXE.1"),
                ResearchPage.infusion("SKULLAXE")
            )
            .setParents("NETHERSHARDS", "THAUMIUM", "INFUSION")
            .setConcealed()
            .setWarp(1)
            .registerResearchItem();
    }
}

package dev.visfox.forbiddenreforged;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
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

        new FmResearchItem(
            "NETHERSHARDS", "FORBIDDEN", new AspectList(),
            -8, -5, 0, TcStack.of("forbiddenmagic:wrath_shard")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.1"),
                ResearchPage.text(FmConfig.lustDisabled()
                    ? "forbiddenmagic.research_page.NETHERSHARDS.2b"
                    : "forbiddenmagic.research_page.NETHERSHARDS.2"),
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.3")
            )
            .setStub().setRound().setAutoUnlock()
            .registerResearchItem();

        new FmResearchItem(
            "TAINTTREE", "FORBIDDEN",
            new AspectList().add(Aspect.TREE, 4).add(Aspect.TAINT, 6).add(Aspect.POISON, 2).add(Aspect.PLANT, 3),
            -11, 3, 3, TcStack.of("forbiddenmagic:taint_sapling")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.TAINTTREE.1"),
                ResearchPage.crucible("FM_TAINTTREE")
            )
            .setParents("NETHERSHARDS")
            .setConcealed()
            .registerResearchItem();

        new FmResearchItem(
            "TAINTSTONE", "FORBIDDEN",
            new AspectList().add(Aspect.EARTH, 4).add(Aspect.TAINT, 6).add(Aspect.ORDER, 2),
            -10, 3, 2, TcStack.of("forbiddenmagic:taint_stone_brick")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.TAINTSTONE.1"),
                ResearchPage.arcane("FM_TAINTSTONE"),
                ResearchPage.crafting("TaintBrick")
            )
            .setParents("NETHERSHARDS")
            .setConcealed()
            .registerResearchItem();

        new FmResearchItem(
            "BLACKFLOWER", "FORBIDDEN",
            new AspectList().add(Aspect.PLANT, 3).add(Aspect.SENSES, 2).add(Aspect.DARKNESS, 4),
            -2, -6, 1, TcStack.of("forbiddenmagic:umbral_rose")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.BLACKFLOWER.1"),
                ResearchPage.crucible("FM_BLACKFLOWER"),
                ResearchPage.crafting("BlackInk")
            )
            .setAspectTriggers(Aspect.SENSES)
            .registerResearchItem();

        new FmResearchItem(
            "ARCANECAKE", "FORBIDDEN",
            new AspectList().add(FmAspects.GLUTTONY, 6).add(Aspect.HUNGER, 3).add(Aspect.CRAFT, 2),
            -12, -6, 2, TcStack.of("forbiddenmagic:arcane_cake")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.ARCANECAKE.1"),
                ResearchPage.infusion("FM_ARCANECAKE")
            )
            .setParents("NETHERSHARDS")
            .setAspectTriggers(FmAspects.GLUTTONY)
            .setConcealed()
            .registerResearchItem();

        new FmResearchItem(
            "ROD_tainted", "FORBIDDEN",
            new AspectList().add(Aspect.TAINT, 4).add(Aspect.MAGIC, 2),
            -10, -4, 1, TcStack.of("forbiddenmagic:tainted_rod")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.ROD_tainted.1"),
                ResearchPage.infusion("FM_ROD_TAINTED")
            )
            .setParents("NETHERSHARDS")
            .setHidden()
            .registerResearchItem();

        new FmResearchItem(
            "ROD_infernal", "FORBIDDEN",
            new AspectList().add(FmAspects.NETHER, 4).add(Aspect.FIRE, 3).add(Aspect.TOOL, 1),
            -11, -4, 3, TcStack.of("forbiddenmagic:infernal_rod")
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.ROD_infernal.1"),
                ResearchPage.text("forbiddenmagic.research_page.ROD_infernal.2"),
                ResearchPage.infusion("FM_ROD_INFERNAL")
            )
            .setParents("NETHERSHARDS")
            .setHidden()
            .registerResearchItem();
    }
}

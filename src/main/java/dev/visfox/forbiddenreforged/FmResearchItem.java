package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.compat.crafting.TcStack;
import dev.visfox.forbiddenreforged.compat.research.ResearchItem;

import net.minecraft.resources.ResourceLocation;

import thaumcraft.api.aspects.AspectList;

public final class FmResearchItem extends ResearchItem {
    public FmResearchItem(
            String key,
            String category,
            AspectList tags,
            int column,
            int row,
            int complexity,
            TcStack icon) {
        super(key, category, tags, column, row, complexity, icon);
    }

    public FmResearchItem(
            String key,
            String category,
            AspectList tags,
            int column,
            int row,
            int complexity,
            ResourceLocation icon) {
        super(key, category, tags, column, row, complexity, icon);
    }

    @Override
    public String textKey() {
        return FmConfig.tagResearchEnabled()
                ? "forbiddenmagic.research_text." + key()
                : super.textKey();
    }
}

package dev.visfox.forbiddenreforged.compat.research;

import dev.visfox.forbiddenreforged.compat.crafting.TcStack;

import net.minecraft.resources.ResourceLocation;

import thaumcraft.api.aspects.*;

public class ResearchItem extends thaumcraft.api.research.ResearchItem {
    public ResearchItem(
            String key, String cat, AspectList tags, int x, int y, int complexity, TcStack icon) {
        super(key, cat, tags, x, y, complexity, icon.stack());
    }

    public ResearchItem(
            String key,
            String cat,
            AspectList tags,
            int x,
            int y,
            int complexity,
            ResourceLocation icon) {
        super(key, cat, tags, x, y, complexity, icon);
    }

    public String key() {
        return key;
    }

    public String textKey() {
        return "tc.research_text." + key;
    }

    @Override
    public String getName() {
        return net.minecraft.network.chat.Component.translatable("tc.research_name." + key)
                .getString();
    }

    @Override
    public String getText() {
        return net.minecraft.network.chat.Component.translatable(textKey()).getString();
    }

    public ResearchItem setWarp(int amount) {
        Warp.RESEARCH.put(key, amount);
        return this;
    }

    @Override
    public ResearchItem setStub() {
        super.setStub();
        return this;
    }

    @Override
    public ResearchItem setRound() {
        super.setRound();
        return this;
    }

    @Override
    public ResearchItem setAutoUnlock() {
        super.setAutoUnlock();
        return this;
    }

    @Override
    public ResearchItem setHidden() {
        super.setHidden();
        return this;
    }

    @Override
    public ResearchItem setConcealed() {
        super.setConcealed();
        return this;
    }

    @Override
    public ResearchItem setSecondary() {
        super.setSecondary();
        return this;
    }

    @Override
    public ResearchItem setSpecial() {
        super.setSpecial();
        return this;
    }

    @Override
    public ResearchItem setLost() {
        super.setLost();
        return this;
    }

    @Override
    public ResearchItem setVirtual() {
        super.setVirtual();
        return this;
    }

    @Override
    public ResearchItem setParents(String... args) {
        super.setParents(args);
        return this;
    }

    @Override
    public ResearchItem setParentsHidden(String... args) {
        super.setParentsHidden(args);
        return this;
    }

    @Override
    public ResearchItem setAspectTriggers(Aspect... args) {
        super.setAspectTriggers(args);
        return this;
    }

    @Override
    public ResearchItem setPages(thaumcraft.api.research.ResearchPage... args) {
        super.setPages(args);
        return this;
    }
}

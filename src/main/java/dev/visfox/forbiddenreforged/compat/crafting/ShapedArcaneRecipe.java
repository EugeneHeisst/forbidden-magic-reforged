package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public record ShapedArcaneRecipe(
        String key,
        String research,
        TcStack output,
        AspectList aspects,
        int width,
        int height,
        List<TcIngredient> ingredients) {}

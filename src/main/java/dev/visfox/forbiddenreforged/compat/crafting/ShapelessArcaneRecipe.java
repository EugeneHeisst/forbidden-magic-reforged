package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public record ShapelessArcaneRecipe(
        String key,
        String research,
        TcStack output,
        AspectList aspects,
        List<TcIngredient> ingredients) {}

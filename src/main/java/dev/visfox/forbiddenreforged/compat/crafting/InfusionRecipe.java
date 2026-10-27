package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public record InfusionRecipe(
        String key,
        String research,
        TcStack output,
        int instability,
        AspectList aspects,
        TcIngredient central,
        List<TcIngredient> components) {}

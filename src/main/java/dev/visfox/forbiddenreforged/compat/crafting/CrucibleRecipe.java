package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public record CrucibleRecipe(
        String key, String research, TcStack output, TcIngredient catalyst, AspectList aspects) {}

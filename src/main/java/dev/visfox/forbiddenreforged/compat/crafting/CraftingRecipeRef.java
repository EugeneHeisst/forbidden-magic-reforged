package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public record CraftingRecipeRef(
        String key, String research, ResourceLocation recipe, TcStack output) {}

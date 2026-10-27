package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;

import thaumcraft.api.aspects.*;

import java.util.*;

public record TcStack(String id, int count) {
    public static TcStack of(String id, int count) {
        return new TcStack(id, count);
    }

    public ItemStack stack() {
        return new ItemStack(TcIngredient.item(id).item(), count);
    }
}

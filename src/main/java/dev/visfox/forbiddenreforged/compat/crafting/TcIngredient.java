package dev.visfox.forbiddenreforged.compat.crafting;

import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import thaumcraft.api.aspects.*;

import java.util.*;

public record TcIngredient(String id, boolean tag) {
    public static final TcIngredient EMPTY = new TcIngredient("", false);

    public static TcIngredient item(String id) {
        return new TcIngredient(id, false);
    }

    public static TcIngredient tag(String id) {
        return new TcIngredient(id, true);
    }

    public static ResourceLocation location(String id) {
        var r = ResourceLocation.parse(id);
        if (r.getNamespace().equals("thaumcraft")) {
            String path = r.getPath();
            return ResourceLocation.fromNamespaceAndPath("tc4modern", path);
        }
        return r;
    }

    public Ingredient ingredient() {
        if (id.isEmpty()) return Ingredient.EMPTY;
        return tag
                ? Ingredient.of(net.minecraft.tags.TagKey.create(Registries.ITEM, location(id)))
                : Ingredient.of(item());
    }

    public Item item() {
        var r = location(id);
        if (!BuiltInRegistries.ITEM.containsKey(r))
            throw new IllegalStateException("Missing Forbidden Magic ingredient " + r);
        return BuiltInRegistries.ITEM.get(r);
    }
}

package dev.visfox.forbiddenreforged.compat.crafting;

import dev.eugheist.tc4modern.api.TC4Addons;

import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import thaumcraft.api.aspects.*;

import java.util.*;

public final class TcRecipes {
    public static final List<thaumcraft.common.tiles.infusion.InfusionEnchantments.Recipe>
            ENCHANTMENTS = new ArrayList<>();
    public static final Map<String, String> ENCHANTMENT_RESEARCH = new HashMap<>();

    public static void register(InfusionRecipe r) {
        TC4Addons.addInfusionRecipe(
                new dev.eugheist.tc4modern.InfusionItemRecipes.Recipe(
                        r.key(),
                        research(r.research()),
                        () -> r.output().stack().getItem(),
                        r.instability(),
                        r.aspects(),
                        r.central()::item,
                        r.components().stream()
                                .<java.util.function.Supplier<Item>>map(c -> c::item)
                                .toList()));
        TC4Addons.registerInfusionOutput(r.key(), input -> r.output().stack());
    }

    public static void register(CrucibleRecipe r) {
        TC4Addons.addCrucibleRecipe(
                r.key(),
                new thaumcraft.api.crafting.CrucibleRecipe(
                        research(r.research()),
                        r.output().stack(),
                        r.catalyst().ingredient(),
                        r.aspects()));
    }

    public static void register(InfusionEnchantmentRecipe r) {
        ENCHANTMENT_RESEARCH.put("InfEnchFM" + r.key(), r.research());
        ENCHANTMENTS.add(
                new thaumcraft.common.tiles.infusion.InfusionEnchantments.Recipe(
                        "InfEnchFM" + r.key(),
                        net.minecraft.resources.ResourceKey.create(
                                Registries.ENCHANTMENT, r.enchantment()),
                        r.instability(),
                        r.aspects(),
                        r.components().stream()
                                .<java.util.function.Supplier<Item>>map(c -> c::item)
                                .toList()));
    }

    public static final Map<String, String> CRAFTING = new HashMap<>();

    public static void register(CraftingRecipeRef r) {
        CRAFTING.put(r.key(), r.recipe().toString());
    }

    public static String research(String key) {
        return switch (key) {
            case "ROD_TAINTED" -> "ROD_tainted";
            case "ROD_INFERNAL" -> "ROD_infernal";
            default -> key;
        };
    }

    public static void register(ShapedArcaneRecipe r) {
        arcane(
                r.key(),
                r.research(),
                r.output(),
                r.aspects(),
                r.width(),
                r.height(),
                r.ingredients(),
                false);
    }

    public static void register(ShapelessArcaneRecipe r) {
        arcane(r.key(), r.research(), r.output(), r.aspects(), 3, 3, padded(r.ingredients()), true);
    }

    private static List<TcIngredient> padded(List<TcIngredient> parts) {
        var out = new ArrayList<>(parts);
        while (out.size() < 9) out.add(TcIngredient.EMPTY);
        return out;
    }

    private static void arcane(
            String key,
            String research,
            TcStack output,
            AspectList aspects,
            int width,
            int height,
            List<TcIngredient> parts,
            boolean shapeless) {
        var ingredients = parts.stream().map(TcIngredient::ingredient).toList();
        var predicates = new ArrayList<java.util.function.Predicate<ItemStack>>();
        for (var i : ingredients) predicates.add(i == Ingredient.EMPTY ? null : i::test);
        var shape =
                new thaumcraft.api.crafting.ArcanePattern<ItemStack>(
                                research, width, height, predicates)
                        .setMirrored(true)
                        .setShapeless(shapeless);
        TC4Addons.addArcaneRecipe(
                new dev.eugheist.tc4modern.ArcaneRecipes.Entry(
                        key, research, width, height, ingredients, output.stack(), aspects, shape));
    }
}

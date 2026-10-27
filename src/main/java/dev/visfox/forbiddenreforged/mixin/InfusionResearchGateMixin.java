package dev.visfox.forbiddenreforged.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = dev.eugheist.tc4modern.InfusionContent.MatrixEntity.class, remap = false)
public abstract class InfusionResearchGateMixin {
    @WrapOperation(
            method = "findRecipe",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lthaumcraft/common/tiles/infusion/InfusionEnchantments$Recipe;acceptsCentral(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Holder;)Z"))
    private boolean gate(
            thaumcraft.common.tiles.infusion.InfusionEnchantments.Recipe recipe,
            net.minecraft.world.item.ItemStack central,
            net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment,
            Operation<Boolean> original,
            @Local(argsOnly = true) net.minecraft.world.entity.player.Player player) {
        var key =
                dev.visfox.forbiddenreforged.compat.crafting.TcRecipes.ENCHANTMENT_RESEARCH.get(
                        recipe.key());
        return (key == null
                        || dev.eugheist.tc4modern.ResearchProgress.completed(
                                        player.getPersistentData())
                                .contains(key))
                && original.call(recipe, central, enchantment);
    }
}

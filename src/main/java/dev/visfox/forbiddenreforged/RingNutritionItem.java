package dev.visfox.forbiddenreforged;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public class RingNutritionItem extends Item implements Bauble, RunicArmor {
    public RingNutritionItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.RING;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player && wearer.level().getGameTime() % 40 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false));
        }
    }

    @Override
    public void onEquipped(ItemStack stack, LivingEntity wearer) {
    }

    @Override
    public void onUnequipped(ItemStack stack, LivingEntity wearer) {
    }
}

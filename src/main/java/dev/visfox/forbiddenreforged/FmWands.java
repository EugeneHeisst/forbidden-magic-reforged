package dev.visfox.forbiddenreforged;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.wand.RodUpdate;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;

public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        WandRegistry.registerRod("tainted", 150, ForbiddenMagic.id("tainted_rod"), 12,
            new TaintedUpdate(), false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_tainted.png"));
        WandRegistry.registerRod("infernal", 150, ForbiddenMagic.id("infernal_rod"), 12,
            new InfernalUpdate(), false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_infernal.png"));
        WandRegistry.registerRod("profane", 50, ForbiddenMagic.id("profane_rod"), 12,
            null, false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_profane.png"));
        WandRegistry.registerRod("profaned", 1000, ResourceLocation.withDefaultNamespace("bedrock"), 1000,
            null, false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_profaned.png"));
        WandRegistry.registerRod("witchwood", 100, ForbiddenMagic.id("witchwood_rod"), 12,
            null, false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_witchwood.png"));
        WandRegistry.registerRod("witchwood", 50, ForbiddenMagic.id("witchwood_staff_rod"), 24,
            null, false, true, false,
            ForbiddenMagic.id("textures/model/wand_rod_witchwood_staff.png"));
    }

    private static final class TaintedUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            if (player.tickCount % 100 != 0 || !TaintBiomes.isTainted(player.level(), player.blockPosition())) {
                return;
            }
            int max = Wands.getMaxVis(stack);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (Wands.getVis(stack, aspect) < max / 10) {
                    Wands.addVis(stack, aspect, 1, true);
                }
            }
        }
    }

    private static final class InfernalUpdate implements RodUpdate {
        private static final List<Aspect> NON_FIRE =
            List.of(Aspect.ORDER, Aspect.ENTROPY, Aspect.AIR, Aspect.EARTH, Aspect.WATER);

        @Override
        public void onUpdate(ItemStack stack, Player player) {
            if (player.tickCount % 100 == 0) {
                int max = Wands.getMaxVis(stack);
                if (player.level().dimension() == Level.NETHER) {
                    for (Aspect aspect : NON_FIRE) {
                        if (Wands.getVis(stack, aspect) < max / 10) {
                            Wands.addVis(stack, aspect, 1, true);
                        }
                    }
                }
                if (Wands.getVis(stack, Aspect.FIRE) < max / 5) {
                    Wands.addVis(stack, Aspect.FIRE, 1, true);
                }
            }
            if (player.isOnFire()) {
                player.clearFire();
            }
            if (player.hasEffect(MobEffects.WITHER)) {
                player.removeEffect(MobEffects.WITHER);
            }
        }
    }
}

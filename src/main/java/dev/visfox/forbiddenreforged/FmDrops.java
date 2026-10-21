package dev.visfox.forbiddenreforged;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import thaumcraft.entity.boss.ThaumcraftBoss;
import thaumcraft.wand.Wands;

public final class FmDrops {
    private FmDrops() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FmDrops::onLivingDrops);
    }

    public static boolean rollWrath(int bonus, int roll) {
        return roll <= bonus;
    }

    public static int prideCount(int looting, int roll) {
        return 2 + roll;
    }

    public static boolean rollSloth(int roll) {
        return roll < 4;
    }

    public static int slothCount(int roll) {
        return 1 + roll;
    }

    public static boolean rollGreed(int greed, int roll) {
        return greed > 0 && roll <= greed;
    }

    public static ItemStack wrathShardStack() {
        return new ItemStack(FmItems.DEADLY_SHARDS.get("wrath").get());
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().dimension() != Level.NETHER) {
            return;
        }
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        boolean playerKill = event.isRecentlyHit() && attacker instanceof ServerPlayer && !(attacker instanceof FakePlayer);
        if (!playerKill) {
            if (rollSloth(entity.getRandom().nextInt(30))) {
                addDrop(event, FmItems.DEADLY_SHARDS.get("sloth").get(), slothCount(entity.getRandom().nextInt(3)));
            }
            return;
        }
        ServerPlayer player = (ServerPlayer) attacker;
        if (entity instanceof Enemy || isBoss(entity)) {
            ItemStack held = player.getMainHandItem();
            int wrath = wrathBonus(player, entity);
            if (rollWrath(wrath, entity.getRandom().nextInt(61))) {
                addDrop(event, FmItems.DEADLY_SHARDS.get("wrath").get(), 1);
            }
            if (isBoss(entity)) {
                int looting = enchant(player, held, Enchantments.LOOTING);
                addDrop(event, FmItems.DEADLY_SHARDS.get("pride").get(), prideCount(looting, entity.getRandom().nextInt(1 + looting)));
            }
            int greed = enchant(player, held, Enchantments.LOOTING) + Wands.getFocusTreasure(held);
            if (rollGreed(greed, entity.getRandom().nextInt(20))) {
                addDrop(event, FmItems.DEADLY_SHARDS.get("greed").get(), 1);
            }
        }
        if (entity instanceof ZombifiedPiglin pig && pig.getMainHandItem().is(FmItems.DEADLY_SHARDS.get("envy").get())) {
            addDrop(event, FmItems.DEADLY_SHARDS.get("envy").get(), 1);
        }
    }

    private static boolean isBoss(LivingEntity entity) {
        return entity instanceof WitherBoss || entity instanceof EnderDragon || entity instanceof ThaumcraftBoss;
    }

    private static int wrathBonus(ServerPlayer player, LivingEntity target) {
        ItemStack held = player.getMainHandItem();
        int wrath = 2;
        if (held.isEmpty()) {
            return wrath;
        }
        wrath += Math.max(0, (int) player.getAttributeValue(Attributes.ATTACK_DAMAGE) - 1);
        wrath += enchant(player, held, Enchantments.SHARPNESS);
        wrath += enchant(player, held, Enchantments.POWER);
        if (!target.fireImmune()) {
            wrath += enchant(player, held, Enchantments.FIRE_ASPECT);
            wrath += enchant(player, held, Enchantments.FLAME);
        }
        if (isTagged(target.getType(), EntityTypeTags.SENSITIVE_TO_SMITE)) {
            wrath += enchant(player, held, Enchantments.SMITE);
        }
        if (isTagged(target.getType(), EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            wrath += enchant(player, held, Enchantments.BANE_OF_ARTHROPODS);
        }
        wrath += Wands.getFocusPotency(held);
        return wrath;
    }

    private static boolean isTagged(EntityType<?> type, TagKey<EntityType<?>> tag) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).is(tag);
    }

    private static int enchant(LivingEntity entity, ItemStack stack, ResourceKey<Enchantment> key) {
        return stack.getEnchantmentLevel(entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key));
    }

    private static void addDrop(LivingDropsEvent event, Item item, int count) {
        LivingEntity entity = event.getEntity();
        ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(item, count));
        drop.setPickUpDelay(10);
        event.getDrops().add(drop);
    }
}

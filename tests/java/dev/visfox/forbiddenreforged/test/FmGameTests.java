package dev.visfox.forbiddenreforged.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspectTags;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.FmDrops;
import dev.visfox.forbiddenreforged.FmEnchantmentEffects;
import dev.visfox.forbiddenreforged.FmEnchantments;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmRecipes;
import dev.visfox.forbiddenreforged.FmTinkerer;
import dev.visfox.forbiddenreforged.MorphState;
import dev.visfox.forbiddenreforged.MorphToolItem;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);
    private static final String[] TESTS = {"mod_loads_and_host_is_present", "aspect_registry_keeps_builtin_order_and_combines", "addon_aspect_survives_client_sync", "sin_aspects_registered_with_own_textures", "sin_aspect_tags_merge_with_existing", "entity_aspect_tags_merge_with_existing", "every_tag_target_resolved", "shards_are_registered_with_seven_ids", "no_lust_removes_luxuria_items", "mob_killed_by_player_drops_wrath_shard", "boss_drops_pride_shards", "unattended_death_drops_sloth_shard", "forbidden_category_and_shard_entries_resolve", "skull_axe_infusion_recipe_matches_original", "every_ported_recipe_resolves_its_inputs", "chain_from_netherrack_to_wrath_shard", "enchantments_resolve_with_expected_shape", "emerald_fragment_is_registered", "consuming_destroys_garbage", "corrupting_yields_sin_shards", "greedy_drops_match_kills", "educational_multiplies_experience", "wrath_adds_damage", "enchantment_research_present", "enchantment_recipes_cover_five", "tinkerer_integration_is_optional", "morph_tools_are_registered", "morph_phase_rotates_sets", "impact_covers_a_three_by_three_plane", "voidtouched_repairs_and_warps", "morph_enchantments_and_exclusivity", "cluster_replaces_drops_with_clusters", "morph_research_entries_present", "morph_recipes_match_the_original"};

    static {
        FUNCTIONS.register(TESTS[0], () -> FmGameTests::modLoadsAndHostIsPresent);
        FUNCTIONS.register(TESTS[1], () -> FmGameTests::aspectRegistryKeepsBuiltinOrderAndCombines);
        FUNCTIONS.register(TESTS[2], () -> FmGameTests::addonAspectSurvivesClientSync);
        FUNCTIONS.register(TESTS[3], () -> FmGameTests::sinAspectsRegisteredWithOwnTextures);
        FUNCTIONS.register(TESTS[4], () -> FmGameTests::sinAspectTagsMergeWithExisting);
        FUNCTIONS.register(TESTS[5], () -> FmGameTests::entityAspectTagsMergeWithExisting);
        FUNCTIONS.register(TESTS[6], () -> FmGameTests::everyTagTargetResolved);
        FUNCTIONS.register(TESTS[7], () -> FmGameTests::shardsAreRegisteredWithSevenIds);
        FUNCTIONS.register(TESTS[8], () -> FmGameTests::noLustRemovesLuxuriaItems);
        FUNCTIONS.register(TESTS[9], () -> FmGameTests::mobKilledByPlayerDropsWrathShard);
        FUNCTIONS.register(TESTS[10], () -> FmGameTests::bossDropsPrideShards);
        FUNCTIONS.register(TESTS[11], () -> FmGameTests::unattendedDeathDropsSlothShard);
        FUNCTIONS.register(TESTS[12], () -> FmGameTests::forbiddenCategoryAndShardEntriesResolve);
        FUNCTIONS.register(TESTS[13], () -> FmGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[14], () -> FmGameTests::everyPortedRecipeResolvesItsInputs);
        FUNCTIONS.register(TESTS[15], () -> FmGameTests::chainFromNetherrackToWrathShard);
        FUNCTIONS.register(TESTS[16], () -> FmGameTests::enchantmentsResolveWithExpectedShape);
        FUNCTIONS.register(TESTS[17], () -> FmGameTests::emeraldFragmentIsRegistered);
        FUNCTIONS.register(TESTS[18], () -> FmGameTests::consumingDestroysGarbage);
        FUNCTIONS.register(TESTS[19], () -> FmGameTests::corruptingYieldsSinShards);
        FUNCTIONS.register(TESTS[20], () -> FmGameTests::greedyDropsMatchKills);
        FUNCTIONS.register(TESTS[21], () -> FmGameTests::educationalMultipliesExperience);
        FUNCTIONS.register(TESTS[22], () -> FmGameTests::wrathAddsDamage);
        FUNCTIONS.register(TESTS[23], () -> FmGameTests::enchantmentResearchPresent);
        FUNCTIONS.register(TESTS[24], () -> FmGameTests::enchantmentRecipesCoverFive);
        FUNCTIONS.register(TESTS[25], () -> FmGameTests::tinkererIntegrationIsOptional);
        FUNCTIONS.register(TESTS[26], () -> FmGameTests::morphToolsAreRegistered);
        FUNCTIONS.register(TESTS[27], () -> FmGameTests::morphPhaseRotatesSets);
        FUNCTIONS.register(TESTS[28], () -> FmGameTests::impactCoversAThreeByThreePlane);
        FUNCTIONS.register(TESTS[29], () -> FmGameTests::voidtouchedRepairsAndWarps);
        FUNCTIONS.register(TESTS[30], () -> FmGameTests::morphEnchantmentsAndExclusivity);
        FUNCTIONS.register(TESTS[31], () -> FmGameTests::clusterReplacesDropsWithClusters);
        FUNCTIONS.register(TESTS[32], () -> FmGameTests::morphResearchEntriesPresent);
        FUNCTIONS.register(TESTS[33], () -> FmGameTests::morphRecipesMatchTheOriginal);
    }

    private FmGameTests() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_default"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    private static void modLoadsAndHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "thaumcraft must be loaded");
        helper.succeed();
    }

    private static void aspectRegistryKeepsBuiltinOrderAndCombines(GameTestHelper helper) {
        Aspect[] all = Aspect.values();
        helper.assertValueEqual(Aspect.builtinCount(), 48, "exactly 48 builtin aspects");
        helper.assertTrue(all.length >= 48, "builtins must remain registered");
        helper.assertValueEqual(all[0], Aspect.AIR, "first builtin must stay index 0");
        helper.assertValueEqual(all[47], Aspect.TRAP, "last builtin must stay index 47");
        Aspect fire = Aspect.combine(Aspect.AIR, Aspect.FIRE);
        helper.assertTrue(fire == Aspect.LIGHT, "combine(AIR, FIRE) must be LIGHT");
        helper.assertTrue(Aspect.getAspect("aer") == Aspect.AIR, "by-tag lookup must work");
        helper.succeed();
    }

    private static void addonAspectSurvivesClientSync(GameTestHelper helper) {
        Aspect nether = FmAspects.NETHER;
        helper.assertTrue(nether != null && nether.ordinal() >= Aspect.builtinCount(), "infernus must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("infernus"), nether, "by-tag lookup must find the add-on aspect");
        AspectList list = new AspectList().add(nether, 3);
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(nether), 3, "aspect amount must survive a network round trip");
        helper.succeed();
    }

    private static void sinAspectsRegisteredWithOwnTextures(GameTestHelper helper) {
        helper.assertTrue(FmAspects.available(), "add-on aspect API missing: host too old");
        Aspect[] sins = {FmAspects.NETHER, FmAspects.WRATH, FmAspects.ENVY, FmAspects.GLUTTONY, FmAspects.PRIDE, FmAspects.SLOTH};
        for (Aspect aspect : sins) {
            helper.assertTrue(aspect != null, "sin aspect must be registered");
            helper.assertTrue(aspect.ordinal() >= Aspect.builtinCount(), aspect.getSerializedName() + " must be an add-on aspect");
            helper.assertTrue(aspect.getImage().getNamespace().equals(ForbiddenMagic.MODID), aspect.getSerializedName() + " texture must live in the add-on namespace");
        }
        Aspect ira = Aspect.combine(Aspect.WEAPON, Aspect.FIRE);
        helper.assertTrue(ira == FmAspects.WRATH, "combine(WEAPON, FIRE) must resolve to the add-on ira");
        helper.assertTrue(Aspect.combine(Aspect.AIR, Aspect.FIRE) == Aspect.LIGHT, "adding ira must not disturb builtin combine");
        int expected = FmConfig.lustDisabled() ? 6 : 7;
        helper.assertValueEqual(Aspect.count(), Aspect.builtinCount() + expected, FmConfig.lustDisabled() ? "six add-on aspects: luxuria is switched off" : "exactly seven add-on aspects");
        helper.succeed();
    }

    private static void sinAspectTagsMergeWithExisting(GameTestHelper helper) {
        AspectRegistry active = AspectRegistry.active();
        AspectList netherrack = active.getObjectTags(Items.NETHERRACK);
        helper.assertTrue(netherrack != null && netherrack.getAmount(FmAspects.NETHER) >= 1, "netherrack must carry infernus");
        helper.assertTrue(netherrack.getAmount(Aspect.FIRE) >= 1, "netherrack must keep its built-in fire aspect");
        AspectList star = active.getObjectTags(Items.NETHER_STAR);
        helper.assertTrue(star != null && star.getAmount(FmAspects.NETHER) >= 8 && star.getAmount(FmAspects.PRIDE) >= 8, "nether star must carry infernus 8 and superbia 8");
        helper.succeed();
    }

    private static void entityAspectTagsMergeWithExisting(GameTestHelper helper) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse("thaumcraft:brainy_zombie")).orElse(null);
        helper.assertTrue(type != null, "brainy zombie type must exist");
        Entity entity = type.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        helper.assertTrue(entity != null, "brainy zombie must be creatable");
        AspectList tags = AspectRegistry.active().getEntityTags(entity);
        helper.assertTrue(tags != null && tags.getAmount(Aspect.UNDEAD) >= 3, "brainy zombie keeps its base undead tag");
        helper.assertTrue(tags.getAmount(FmAspects.WRATH) >= 3, "brainy zombie gains wrath from the FM table");
        helper.succeed();
    }

    private static void everyTagTargetResolved(GameTestHelper helper) {
        List<String> unresolved = new ArrayList<>();
        for (String target : FmAspectTags.declaredTargets()) {
            if (target.startsWith("entity:")) {
                if (BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(target.substring("entity:".length()))).isEmpty()) {
                    unresolved.add(target);
                }
            } else if (target.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1)));
                boolean found = false;
                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    found = true;
                    break;
                }
                if (!found) {
                    unresolved.add(target);
                }
            } else if (BuiltInRegistries.ITEM.getOptional(Identifier.parse(target)).isEmpty()) {
                unresolved.add(target);
            }
        }
        helper.assertTrue(unresolved.isEmpty(), "unresolved aspect tag targets: " + unresolved);
        helper.succeed();
    }

    private static void shardsAreRegisteredWithSevenIds(GameTestHelper helper) {
        helper.assertValueEqual(FmItems.DEADLY_SHARDS.size(), 7, "the original registers seven vices");
        for (var entry : FmItems.DEADLY_SHARDS.entrySet()) {
            if (entry.getKey().equals("lust") && FmConfig.lustDisabled()) {
                continue;
            }
            Item item = entry.getValue().get();
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ForbiddenMagic.MODID), entry.getKey() + " must be namespaced to the port");
            AspectList tags = AspectRegistry.active().getObjectTags(item);
            helper.assertTrue(tags != null && tags.size() > 0, entry.getKey() + " must carry an aspect tag");
        }
        helper.succeed();
    }

    private static void noLustRemovesLuxuriaItems(GameTestHelper helper) {
        ForbiddenMagic.LOGGER.info("no_lust gate: disabled={}, luxuria={}", FmConfig.lustDisabled(), Aspect.getAspect("luxuria"));
        if (!FmConfig.lustDisabled()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(FmAspects.LUST == null && Aspect.getAspect("luxuria") == null, "luxuria must be absent from the registry");
        helper.assertFalse(FmCreativeTab.visible("lust"), "lust shard must not be offered to the creative tab");
        AspectList tags = AspectRegistry.active().getObjectTags(FmItems.DEADLY_SHARDS.get("lust").get());
        helper.assertTrue(tags == null || tags.size() == 0, "lust shard must carry no aspect tag");
        helper.succeed();
    }

    private static void mobKilledByPlayerDropsWrathShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollWrath(2, 2), "chance 2 must drop at roll 2, the original uses nextInt(61) <= wrath");
        helper.assertFalse(FmDrops.rollWrath(2, 3), "chance 2 must not drop at roll 3");
        helper.assertValueEqual(FmDrops.wrathShardStack().getItem(), FmItems.DEADLY_SHARDS.get("wrath").get(), "wrath drop must be the wrath shard");
        helper.succeed();
    }

    private static void bossDropsPrideShards(GameTestHelper helper) {
        helper.assertValueEqual(FmDrops.prideCount(0, 0), 2, "boss without looting drops two pride shards");
        helper.assertValueEqual(FmDrops.prideCount(2, 1), 3, "looting 2 with roll 1 gives three");
        helper.succeed();
    }

    private static void unattendedDeathDropsSlothShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollSloth(3), "sloth drops on the last allowed roll");
        helper.assertFalse(FmDrops.rollSloth(4), "sloth must not drop on roll 4");
        helper.assertValueEqual(FmDrops.slothCount(0), 1, "minimum sloth drop is one");
        helper.assertValueEqual(FmDrops.slothCount(2), 3, "maximum sloth drop is three");
        helper.succeed();
    }

    private static void forbiddenCategoryAndShardEntriesResolve(GameTestHelper helper) {
        ResearchItem shards = ResearchCategories.getResearch("NETHERSHARDS");
        helper.assertTrue(shards != null, "NETHERSHARDS must be registered");
        helper.assertValueEqual(shards.category(), "FORBIDDEN", "category");
        helper.assertValueEqual(shards.column(), -8, "column");
        helper.assertValueEqual(shards.row(), -5, "row");
        helper.assertTrue(shards.isStub() && shards.isRound() && shards.isAutoUnlock(), "stub, round and auto-unlock as in the original");
        ResearchItem axe = ResearchCategories.getResearch("SKULLAXE");
        helper.assertTrue(axe != null, "SKULLAXE must be registered");
        helper.assertTrue(axe.isConcealed(), "SKULLAXE must stay concealed");
        helper.assertValueEqual(axe.warp(), 1, "SKULLAXE carries one warp point as in the original");
        helper.assertValueEqual(axe.parents(), List.of("NETHERSHARDS", "THAUMIUM", "INFUSION"), "SKULLAXE parents, in order");
        helper.succeed();
    }

    private static void skullAxeInfusionRecipeMatchesOriginal(GameTestHelper helper) {
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("SKULLAXE");
        helper.assertTrue(recipe != null, "skull axe recipe must be registered");
        helper.assertValueEqual(recipe.instability(), 1, "instability");
        helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.WRATH), 8, "wrath cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.WEAPON), 8, "weapon cost");
        helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.NETHER), 8, "nether cost");
        helper.assertValueEqual(recipe.components().size(), 4, "four components");
        helper.succeed();
    }

    private static void everyPortedRecipeResolvesItsInputs(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (TcRecipe recipe : TcRecipes.all()) {
            if (!FmRecipes.OWN_KEYS.contains(recipe.key())) {
                continue;
            }
            if (recipe instanceof InfusionRecipe infusion) {
                checkIngredient(infusion.central(), missing);
                infusion.components().forEach(ingredient -> checkIngredient(ingredient, missing));
                checkStack(infusion.result(), missing);
            }
        }
        helper.assertTrue(missing.isEmpty(), "unresolved recipe references: " + missing);
        helper.succeed();
    }

    private static void checkIngredient(TcIngredient ingredient, List<String> missing) {
        if (!ingredient.exists()) {
            missing.add(ingredient.toString());
        }
    }

    private static void checkStack(TcStack stack, List<String> missing) {
        if (!stack.exists()) {
            missing.add(stack.item().toString());
        }
    }

    private static void chainFromNetherrackToWrathShard(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        AspectList tags = AspectRegistry.active().getObjectTags(Items.NETHERRACK);
        helper.assertTrue(tags != null && tags.getAmount(FmAspects.NETHER) >= 1, "netherrack yields infernus");
        helper.assertTrue(ResearchCategories.getResearch("NETHERSHARDS") != null, "shard research exists");
        ResearchManager.grantAutoUnlock(player);
        helper.assertTrue(ResearchManager.isComplete(player, "NETHERSHARDS"), "the auto-unlock entry is known after a join");
        ItemStack wrathShard = new ItemStack(FmItems.DEADLY_SHARDS.get("wrath").get());
        AspectList shardTags = AspectRegistry.active().getObjectTags(wrathShard.getItem());
        helper.assertTrue(shardTags != null && shardTags.getAmount(FmAspects.WRATH) >= 2, "the wrath shard carries ira");
        helper.assertTrue(FmDrops.rollWrath(2, 2), "the wrath drop rule fires at the original boundary");
        InfusionRecipe skullAxe = (InfusionRecipe) TcRecipes.get("SKULLAXE");
        helper.assertTrue(skullAxe != null, "skull axe recipe registered");
        helper.assertValueEqual(skullAxe.aspects().getAmount(FmAspects.WRATH), 8, "skull axe costs eight ira");
        helper.succeed();
    }

    private static void enchantmentsResolveWithExpectedShape(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.WRATH).value().getMaxLevel(), 5, "wrath max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.GREEDY).value().getMaxLevel(), 1, "greedy max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.EDUCATIONAL).value().getMaxLevel(), 5, "educational max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CONSUMING).value().getMaxLevel(), 1, "consuming max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CORRUPTING).value().getMaxLevel(), 1, "corrupting max level");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.WRATH).value().isSupportedItem(new ItemStack(Items.DIAMOND_SWORD)), "wrath applies to swords");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.WRATH).value().isSupportedItem(new ItemStack(Items.DIAMOND_AXE)), "wrath applies to axes");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.CONSUMING).value().isSupportedItem(new ItemStack(Items.IRON_SHOVEL)), "consuming applies to shovels");
        helper.assertFalse(registry.getOrThrow(FmEnchantments.CORRUPTING).value().isSupportedItem(new ItemStack(Items.IRON_SHOVEL)), "corrupting is pickaxe only");
        helper.succeed();
    }

    private static void emeraldFragmentIsRegistered(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.EMERALD_FRAGMENT.get());
        helper.assertFalse(stack.isEmpty(), "emerald fragment must exist");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:emerald_fragment"), "emerald fragment id");
        helper.succeed();
    }

    private static void consumingDestroysGarbage(GameTestHelper helper) {
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.DIRT)), "dirt is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.COBBLESTONE)), "cobblestone is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.SAND)), "sand is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.GRAVEL)), "gravel is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.NETHERRACK)), "netherrack is garbage");
        helper.assertFalse(FmEnchantmentEffects.isGarbage(new ItemStack(Items.DIAMOND)), "diamond is not garbage");
        helper.succeed();
    }

    private static void corruptingYieldsSinShards(GameTestHelper helper) {
        RandomSource random = RandomSource.create(1234L);
        java.util.Set<Item> pool = java.util.Set.of(
            FmItems.DEADLY_SHARDS.get("wrath").get(),
            FmItems.DEADLY_SHARDS.get("envy").get(),
            FmItems.DEADLY_SHARDS.get("pride").get(),
            FmItems.DEADLY_SHARDS.get("lust").get(),
            FmItems.DEADLY_SHARDS.get("sloth").get(),
            FmItems.DEADLY_SHARDS.get("greed").get(),
            FmItems.GLUTTONY_SHARD.get()
        );
        for (int roll = 0; roll < 200; roll++) {
            ItemStack shard = FmEnchantmentEffects.sinShard(random);
            helper.assertTrue(pool.contains(shard.getItem()), "sin shard must come from the pool");
            if (FmConfig.lustDisabled()) {
                helper.assertFalse(shard.getItem() == FmItems.DEADLY_SHARDS.get("lust").get(), "no_lust keeps lust out of the pool");
            }
        }
        int hits = 0;
        for (int roll = 0; roll < 300; roll++) {
            if (FmEnchantmentEffects.corruptRoll(random)) {
                hits++;
            }
        }
        helper.assertTrue(hits > 50 && hits < 150, "corruption fires roughly one time in three");
        helper.succeed();
    }

    private static void greedyDropsMatchKills(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.greedyDrop(true, 99).getItem(), Items.EMERALD, "villager drops an emerald");
        helper.assertValueEqual(FmEnchantmentEffects.greedyDrop(false, 2).getItem(), FmItems.EMERALD_FRAGMENT.get(), "hostile mob drops a fragment on a low roll");
        helper.assertTrue(FmEnchantmentEffects.greedyDrop(false, 3).isEmpty(), "hostile mob drops nothing on a high roll");
        helper.succeed();
    }

    private static void educationalMultipliesExperience(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 1), 40, "level 1 adds three times the base xp");
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 5), 160, "level 5 adds fifteen times the base xp");
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(0, 5), 0, "no base xp stays zero");
        helper.succeed();
    }

    private static void wrathAddsDamage(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(1), 1.25F, "level 1 adds 1.25 damage");
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(5), 6.25F, "level 5 adds 6.25 damage");
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(0), 0.0F, "level 0 adds nothing");
        helper.succeed();
    }

    private static void enchantmentResearchPresent(GameTestHelper helper) {
        ResearchItem wrath = ResearchCategories.getResearch("WRATH");
        helper.assertTrue(wrath != null, "wrath research must exist");
        helper.assertValueEqual(wrath.warp(), 3, "wrath research carries warp 3");
        helper.assertTrue(wrath.parents().contains("NETHERSHARDS"), "wrath descends from nether shards");
        helper.assertTrue(wrath.parents().contains("INFUSIONENCHANTMENT"), "wrath needs infusion enchantment");
        helper.assertValueEqual(wrath.pages().size(), 2, "wrath has text and recipe pages");
        for (String key : new String[]{"GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING"}) {
            helper.assertTrue(ResearchCategories.getResearch(key) != null, key + " research must exist");
        }
        helper.succeed();
    }

    private static void enchantmentRecipesCoverFive(GameTestHelper helper) {
        for (String key : new String[]{"WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING"}) {
            TcRecipe recipe = TcRecipes.get(key);
            helper.assertTrue(recipe instanceof InfusionEnchantmentRecipe, key + " must be an infusion enchantment recipe");
            InfusionEnchantmentRecipe infusion = (InfusionEnchantmentRecipe) recipe;
            helper.assertValueEqual(infusion.enchantment(), Identifier.parse("forbiddenmagic:" + key.toLowerCase(java.util.Locale.ROOT)), key + " enchantment id");
        }
        InfusionEnchantmentRecipe wrath = (InfusionEnchantmentRecipe) TcRecipes.get("WRATH");
        helper.assertValueEqual(wrath.instability(), 8, "wrath instability");
        helper.assertValueEqual(wrath.aspects().getAmount(FmAspects.WRATH), 16, "wrath aspect cost");
        helper.succeed();
    }

    private static void tinkererIntegrationIsOptional(GameTestHelper helper) {
        boolean loaded = ModList.get().isLoaded("thaumictinkerer");
        if (!loaded) {
            helper.assertValueEqual(FmTinkerer.registeredCount(), 0, "no TT means no registered costs");
        } else {
            helper.assertValueEqual(FmTinkerer.registeredCount(), 8, "TT receives eight enchantment costs");
        }
        helper.succeed();
    }

    private static void morphToolsAreRegistered(GameTestHelper helper) {
        for (var item : java.util.List.of(FmItems.MORPH_PICKAXE, FmItems.MORPH_SWORD, FmItems.MORPH_SHOVEL, FmItems.MORPH_AXE)) {
            ItemStack stack = new ItemStack(item.get());
            helper.assertValueEqual(stack.getMaxDamage(), 1500, "morph tools use elemental durability");
            helper.assertValueEqual(stack.getRarity(), net.minecraft.world.item.Rarity.EPIC, "morph tools are epic");
            helper.assertTrue(stack.get(DataComponents.REPAIRABLE) != null, "morph tools declare a repair item");
            helper.assertTrue(stack.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(FmItems.DEADLY_SHARDS.get("envy").get())), "envy shards repair morph tools");
        }
        helper.succeed();
    }

    private static void morphPhaseRotatesSets(GameTestHelper helper) {
        MorphState state = MorphState.EMPTY.store(ItemEnchantments.EMPTY, "Sharpness").advance();
        helper.assertValueEqual(state.phase(), 1, "phase advances");
        helper.assertValueEqual(state.names().get(0), "Sharpness", "the old name is kept in slot zero");
        helper.assertValueEqual(state.currentName(), "", "the new slot starts empty");
        MorphState wrapped = state.store(ItemEnchantments.EMPTY, "Smite").advance().advance();
        helper.assertValueEqual(wrapped.phase(), 0, "three phases wrap back to zero");
        helper.assertValueEqual(wrapped.currentName(), "Sharpness", "the first slot name returns");
        helper.succeed();
    }

    private static void impactCoversAThreeByThreePlane(GameTestHelper helper) {
        java.util.List<BlockPos> area = MorphToolItem.impactArea(Direction.UP, BlockPos.ZERO);
        helper.assertValueEqual(area.size(), 9, "impact covers nine blocks");
        helper.assertTrue(area.contains(new BlockPos(-1, 0, 1)) && area.contains(new BlockPos(1, 0, -1)), "impact stays in the horizontal plane");
        helper.assertTrue(MorphToolItem.impactArea(Direction.EAST, BlockPos.ZERO).contains(new BlockPos(0, 1, 1)), "east impact uses the vertical plane");
        helper.assertValueEqual(MorphToolItem.dominantDirection(new net.minecraft.world.phys.Vec3(0.2, -3.0, 0.1)), Direction.DOWN, "look direction maps to a face");
        helper.succeed();
    }

    private static void voidtouchedRepairsAndWarps(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.MORPH_PICKAXE.get());
        ServerPlayer player = mockPlayer(helper);
        MorphToolItem item = (MorphToolItem) stack.getItem();
        helper.assertValueEqual(item.getWarp(stack, player), 0, "plain morph tool carries no warp");
        stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FmEnchantments.VOIDTOUCHED), 1);
        helper.assertValueEqual(item.getWarp(stack, player), 1, "voidtouched morph tool carries warp 1");
        helper.succeed();
    }

    private static void morphEnchantmentsAndExclusivity(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CLUSTER).value().getMaxLevel(), 4, "cluster max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.IMPACT).value().getMaxLevel(), 1, "impact max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.VOIDTOUCHED).value().getMaxLevel(), 1, "voidtouched max level");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.CLUSTER).value().isSupportedItem(new ItemStack(FmItems.MORPH_PICKAXE.get())), "cluster fits the chameleon pickaxe");
        helper.assertFalse(registry.getOrThrow(FmEnchantments.IMPACT).value().isSupportedItem(new ItemStack(FmItems.MORPH_SWORD.get())), "impact does not fit the sword");
        helper.assertFalse(net.minecraft.world.item.enchantment.Enchantment.areCompatible(
            registry.getOrThrow(FmEnchantments.CLUSTER), registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE)),
            "cluster excludes fortune");
        helper.assertFalse(net.minecraft.world.item.enchantment.Enchantment.areCompatible(
            registry.getOrThrow(FmEnchantments.CONSUMING), registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH)),
            "consuming excludes silk touch");
        helper.assertFalse(net.minecraft.world.item.enchantment.Enchantment.areCompatible(
            registry.getOrThrow(FmEnchantments.GREEDY), registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING)),
            "greedy excludes looting");
        helper.succeed();
    }

    private static void clusterReplacesDropsWithClusters(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.clusterChance(1), 0.275F, "level 1 chance");
        helper.assertValueEqual(FmEnchantmentEffects.clusterChance(4), 0.5F, "level 4 chance");
        ItemStack replaced = thaumcraft.item.tool.SpecialMining.find(
            net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState(),
            new ItemStack(Items.RAW_IRON),
            1.0F,
            RandomSource.create(1L));
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(replaced.getItem()), Identifier.parse("thaumcraft:native_iron_cluster"), "iron ore yields a native cluster");
        helper.succeed();
    }

    private static void morphResearchEntriesPresent(GameTestHelper helper) {
        ResearchItem morph = ResearchCategories.getResearch("MORPHTOOLS");
        helper.assertTrue(morph != null, "morph research must exist");
        helper.assertValueEqual(morph.warp(), 4, "morph research carries warp 4");
        helper.assertValueEqual(morph.pages().size(), 5, "morph research has text and four recipes");
        helper.assertTrue(ResearchCategories.getResearch("CLUSTER") != null, "cluster research exists");
        helper.assertTrue(ResearchCategories.getResearch("IMPACT") != null, "impact research exists");
        ResearchItem voidtouched = ResearchCategories.getResearch("VOIDTOUCHED");
        helper.assertTrue(voidtouched != null, "voidtouched research exists");
        helper.assertValueEqual(voidtouched.warp(), 3, "voidtouched research carries warp 3");
        helper.succeed();
    }

    private static void morphRecipesMatchTheOriginal(GameTestHelper helper) {
        for (String key : new String[]{"MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE"}) {
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe != null, key + " recipe must exist");
            helper.assertValueEqual(recipe.instability(), 6, key + " instability");
            helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.ENVY), 16, key + " envy cost");
        }
        for (String key : new String[]{"CLUSTER", "IMPACT", "VOIDTOUCHED"}) {
            helper.assertTrue(TcRecipes.get(key) instanceof InfusionEnchantmentRecipe, key + " must be an enchantment recipe");
        }
        InfusionEnchantmentRecipe voidtouched = (InfusionEnchantmentRecipe) TcRecipes.get("VOIDTOUCHED");
        helper.assertValueEqual(voidtouched.aspects().getAmount(FmAspects.ENVY), 24, "voidtouched envy cost");
        helper.succeed();
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        player.connection.markClientLoaded();
        return player;
    }
}

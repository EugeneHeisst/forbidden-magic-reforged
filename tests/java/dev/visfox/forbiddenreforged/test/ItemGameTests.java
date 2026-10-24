package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.CrystalwellItem;
import dev.visfox.forbiddenreforged.PrimewellItem;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.RidingCropItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.item.ScribingToolsItem;
import net.minecraft.world.item.Rarity;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class ItemGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "skull_axe_infusion_recipe_matches_original",
        "diabolist_fork_research_and_recipe",
        "taint_coal_is_registered_and_is_fuel",
        "taint_coal_burn_time_varies_within_original_range",
        "emerald_fragment_is_registered",
        "riding_crop_is_registered_as_sword",
        "wand_rods_register_as_functional_cores",
        "creative_tab_is_registered_with_fork_icon",
        "creative_tab_lists_every_registered_item",
        "orichalcum_cap_is_registered",
        "diabolist_fork_switches_to_the_spork_model",
        "lust_shard_switches_to_the_despair_model",
        "crystalwell_is_a_scribing_tool_with_original_durability",
        "primewell_is_a_permanent_epic_scribing_tool",
        "scribe_wells_keep_original_recipes_and_research"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> ItemGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[1], () -> ItemGameTests::diabolistForkResearchAndRecipe);
        FUNCTIONS.register(TESTS[2], () -> ItemGameTests::taintCoalIsRegisteredAndIsFuel);
        FUNCTIONS.register(TESTS[3], () -> ItemGameTests::taintCoalBurnTimeVariesWithinOriginalRange);
        FUNCTIONS.register(TESTS[4], () -> ItemGameTests::emeraldFragmentIsRegistered);
        FUNCTIONS.register(TESTS[5], () -> ItemGameTests::ridingCropIsRegisteredAsSword);
        FUNCTIONS.register(TESTS[6], () -> ItemGameTests::wandRodsRegisterAsFunctionalCores);
        FUNCTIONS.register(TESTS[7], () -> ItemGameTests::creativeTabIsRegisteredWithForkIcon);
        FUNCTIONS.register(TESTS[8], () -> ItemGameTests::creativeTabListsEveryRegisteredItem);
        FUNCTIONS.register(TESTS[9], () -> ItemGameTests::orichalcumCapIsRegistered);
        FUNCTIONS.register(TESTS[10], () -> ItemGameTests::diabolistForkSwitchesToTheSporkModel);
        FUNCTIONS.register(TESTS[11], () -> ItemGameTests::lustShardSwitchesToTheDespairModel);
        FUNCTIONS.register(TESTS[12], () -> ItemGameTests::crystalwellIsAScribingToolWithOriginalDurability);
        FUNCTIONS.register(TESTS[13], () -> ItemGameTests::primewellIsAPermanentEpicScribingTool);
        FUNCTIONS.register(TESTS[14], () -> ItemGameTests::scribeWellsKeepOriginalRecipesAndResearch);
    }

    private ItemGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_items"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
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

    private static void diabolistForkResearchAndRecipe(GameTestHelper helper) {
        ResearchItem fork = ResearchCategories.getResearch("FORK");
        helper.assertTrue(fork != null, "FORK research must be registered");
        helper.assertTrue(fork.isSecondary(), "FORK is secondary");
        helper.assertTrue(fork.isConcealed(), "FORK is concealed");
        helper.assertValueEqual(fork.warp(), 0, "FORK has no warp");
        helper.assertValueEqual(fork.parents(), List.of("INFUSION", "NETHERSHARDS", "THAUMIUM"), "FORK parents");
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("FORK");
        helper.assertTrue(recipe != null, "FORK infusion recipe exists");
        helper.assertValueEqual(recipe.instability(), 1, "FORK instability");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.FIRE), 8, "FORK fire cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.MECHANISM), 8, "FORK mechanism cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.ENERGY), 8, "FORK energy cost");
        helper.assertValueEqual(recipe.components().size(), 4, "FORK has 4 components");
        helper.succeed();
    }

    private static void taintCoalIsRegisteredAndIsFuel(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.TAINT_COAL.get());
        helper.assertFalse(stack.isEmpty(), "taint coal must exist");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:taint_coal"), "taint coal id");
        helper.assertTrue(stack.has(DataComponents.COOKING_FUEL), "taint coal is a fuel");
        helper.succeed();
    }

    private static void taintCoalBurnTimeVariesWithinOriginalRange(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.TAINT_COAL.get());
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        helper.assertTrue(fuel != null, "taint coal carries a cooking fuel component");
        LootContext context = new LootContext.Builder(
            new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY)
        ).create(Optional.empty());
        Set<Integer> drawn = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            int burn = fuel.burnTime().get(context, 0);
            helper.assertTrue(burn >= 1 && burn <= 2400, "burn time " + burn + " must stay within 1..2400");
            drawn.add(burn);
        }
        helper.assertTrue(drawn.size() > 1, "burn time must be drawn per use, not a constant");
        helper.assertTrue(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().isPresent(), "taint coal must serialize its fuel component");
        helper.succeed();
    }

    private static void emeraldFragmentIsRegistered(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.EMERALD_FRAGMENT.get());
        helper.assertFalse(stack.isEmpty(), "emerald fragment must exist");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:emerald_fragment"), "emerald fragment id");
        helper.succeed();
    }

    private static void ridingCropIsRegisteredAsSword(GameTestHelper helper) {
        Item crop = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "riding_crop"));
        helper.assertTrue(crop != Items.AIR, "riding crop must be registered");
        helper.assertTrue(crop instanceof RidingCropItem, "riding crop must be RidingCropItem");
        helper.succeed();
    }

    private static void wandRodsRegisterAsFunctionalCores(GameTestHelper helper) {
        WandRod tainted = WandRegistry.findRod("tainted");
        helper.assertTrue(tainted != null, "tainted rod must be registered in WandRegistry");
        helper.assertValueEqual(tainted.capacity(), 150, "tainted capacity");
        helper.assertValueEqual(tainted.craftCost(), 12, "tainted craft cost");
        helper.assertTrue(tainted.onUpdate() != null, "tainted rod has an update behavior");

        WandRod infernal = WandRegistry.findRod("infernal");
        helper.assertTrue(infernal != null && infernal.capacity() == 150, "infernal rod registered with capacity 150");
        helper.assertTrue(infernal.onUpdate() != null, "infernal rod has an update behavior");

        WandRod profane = WandRegistry.findRod("profane");
        helper.assertTrue(profane != null && profane.capacity() == 50, "profane rod registered with capacity 50");
        helper.assertTrue(profane.onUpdate() != null, "profane rod has an update behavior");

        helper.assertTrue(WandRegistry.findRod("profaned") != null, "profaned burnout rod registered");

        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        helper.assertTrue(Wands.isWand(wand), "tainted wand is created");
        helper.assertValueEqual(Wands.getRod(wand).tag(), "tainted", "created wand uses the tainted rod");
        helper.succeed();
    }

    private static void creativeTabIsRegisteredWithForkIcon(GameTestHelper helper) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ForbiddenMagic.id("forbidden"))
            .map(holder -> holder.value())
            .orElse(null);
        helper.assertTrue(tab != null, "the addon registers its own creative tab");
        helper.assertValueEqual(tab.getIconItem().getItem(), FmItems.DIABOLIST_FORK.get(), "the tab icon is the diabolist fork");
        helper.succeed();
    }

    private static void creativeTabListsEveryRegisteredItem(GameTestHelper helper) {
        List<ItemStack> stacks = dev.visfox.forbiddenreforged.FmCreativeTab.stacks();
        java.util.Set<Item> listed = new java.util.HashSet<>();
        for (ItemStack stack : stacks) {
            listed.add(stack.getItem());
        }
        for (var holder : FmItems.ITEMS.getEntries()) {
            helper.assertTrue(listed.contains(holder.get()), "the tab is missing the item " + holder.getId());
        }
        for (var holder : dev.visfox.forbiddenreforged.FmBlocks.BLOCKS.getEntries()) {
            Item blockItem = holder.get().asItem();
            if (blockItem != Items.AIR) {
                helper.assertTrue(listed.contains(blockItem), "the tab is missing the block " + holder.getId());
            }
        }
        long unique = stacks.stream().map(stack -> stack.getItem() + "#" + stack.getComponents()).distinct().count();
        helper.assertValueEqual(stacks.size(), (int) unique, "no duplicate entries in the tab list");
        helper.succeed();
    }

    private static void orichalcumCapIsRegistered(GameTestHelper helper) {
        thaumcraft.wand.WandCap cap = WandRegistry.findCap("orichalcum");
        helper.assertTrue(cap != null, "the creative orichalcum cap is registered");
        helper.assertValueEqual(cap.baseCostModifier(), 0.0F, "orichalcum charges no vis");
        helper.assertValueEqual(cap.craftCost(), 1000, "orichalcum craft cost is 1000");
        helper.assertTrue(cap.specialAspects().isEmpty(), "orichalcum has no special aspects");
        helper.succeed();
    }

    private static void diabolistForkSwitchesToTheSporkModel(GameTestHelper helper) {
        JsonObject definition = readJson(helper, "/assets/forbiddenmagic/items/diabolist_fork.json");
        JsonObject model = definition.getAsJsonObject("model");
        helper.assertValueEqual(model.get("type").getAsString(), "minecraft:condition", "the fork model is conditional");
        helper.assertValueEqual(model.get("property").getAsString(), "forbiddenmagic:spork", "condition reads the spork config");
        helper.assertValueEqual(model.getAsJsonObject("on_true").get("model").getAsString(), "forbiddenmagic:item/diabolist_fork_spork", "on_true is the spork");
        helper.assertValueEqual(model.getAsJsonObject("on_false").get("model").getAsString(), "forbiddenmagic:item/diabolist_fork", "on_false is the ordinary fork");
        JsonObject spork = readJson(helper, "/assets/forbiddenmagic/models/item/diabolist_fork_spork.json");
        helper.assertValueEqual(spork.getAsJsonObject("textures").get("layer0").getAsString(), "forbiddenmagic:item/spork", "spork model uses the spork texture");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/item/spork.png") != null, "spork texture ships with the mod");
        helper.succeed();
    }

    private static void lustShardSwitchesToTheDespairModel(GameTestHelper helper) {
        JsonObject definition = readJson(helper, "/assets/forbiddenmagic/items/lust_shard.json");
        JsonObject model = definition.getAsJsonObject("model");
        helper.assertValueEqual(model.get("type").getAsString(), "minecraft:condition", "the lust shard model is conditional");
        helper.assertValueEqual(model.get("property").getAsString(), "forbiddenmagic:no_lust", "condition reads the no_lust config");
        helper.assertValueEqual(model.getAsJsonObject("on_true").get("model").getAsString(), "forbiddenmagic:item/lust_shard_despair", "on_true is the despair shard");
        helper.assertValueEqual(model.getAsJsonObject("on_false").get("model").getAsString(), "forbiddenmagic:item/lust_shard", "on_false is the lust shard");
        JsonObject despair = readJson(helper, "/assets/forbiddenmagic/models/item/lust_shard_despair.json");
        helper.assertValueEqual(despair.getAsJsonObject("textures").get("layer0").getAsString(), "forbiddenmagic:item/despair_shard", "despair model uses the despair texture");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/item/despair_shard.png") != null, "despair texture ships with the mod");
        helper.succeed();
    }

    private static JsonObject readJson(GameTestHelper helper, String path) {        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream(path)) {
            if (stream == null) {
                helper.fail("missing resource " + path);
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            helper.fail("could not read " + path + ": " + e);
            return new JsonObject();
        }
    }

    private static void crystalwellIsAScribingToolWithOriginalDurability(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.CRYSTALWELL.get());
        helper.assertTrue(stack.getItem() instanceof CrystalwellItem, "crystalwell uses its behavior item");
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "crystalwell can occupy the research table scribe slot");
        int expectedMaxDamage = thaumcraft.Config.researchDifficulty() == -1 ? 50 : 100;
        helper.assertValueEqual(stack.getMaxDamage(), expectedMaxDamage, "crystalwell durability follows research difficulty");
        helper.assertValueEqual(stack.getCount(), 1, "crystalwell is unstackable");
        helper.succeed();
    }

    private static void primewellIsAPermanentEpicScribingTool(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.PRIMEWELL.get());
        helper.assertTrue(stack.getItem() instanceof PrimewellItem, "primewell uses its behavior item");
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "primewell can occupy the research table scribe slot");
        helper.assertValueEqual(stack.getRarity(), Rarity.EPIC, "primewell retains epic rarity");
        helper.assertTrue(stack.getMaxDamage() > 1_000_000_000, "primewell does not wear out during research");
        stack.setDamageValue(1000);
        helper.assertFalse(stack.getItem().isBarVisible(stack), "primewell does not display a misleading durability bar");
        helper.assertTrue(stack.getDamageValue() < stack.getMaxDamage(), "primewell remains usable after repeated scribe operations");
        helper.succeed();
    }

    private static void scribeWellsKeepOriginalRecipesAndResearch(GameTestHelper helper) {
        ShapelessArcaneRecipe crystalwell = (ShapelessArcaneRecipe) TcRecipes.get("CRYSTALWELL");
        helper.assertTrue(crystalwell != null, "crystalwell arcane recipe is registered");
        helper.assertTrue(TcRecipes.get("PRIMEWELL") != null, "primewell arcane recipe is registered");
        helper.assertTrue(ResearchCategories.getResearch("CRYSTALWELL") != null, "crystalwell research is registered");
        helper.assertTrue(ResearchCategories.getResearch("PRIMEWELL") != null, "primewell research is registered");
        helper.assertTrue(crystalwell.ingredients().get(0).test(new ItemStack(ModItems.SCRIBING_TOOLS.get())),
            "crystalwell recipe uses the registered scribing tools item");
        if (thaumcraft.Config.researchDifficulty() == -1) {
            helper.assertValueEqual(crystalwell.ingredients().size(), 4, "easy research uses two arbitrary Thaumcraft shards");
            helper.assertTrue(crystalwell.ingredients().get(2).test(new ItemStack(ModItems.BALANCED_SHARD.get())),
                "the easy recipe accepts any shard through the shard tag");
        } else {
            helper.assertValueEqual(crystalwell.ingredients().size(), 6, "normal research uses a mixed crystal and three balanced shards");
            helper.assertTrue(crystalwell.ingredients().get(2).test(new ItemStack(ModBlocks.MIXED_CRYSTAL_CLUSTER.get().asItem())),
                "the hard recipe uses the mixed crystal cluster");
            helper.assertTrue(crystalwell.ingredients().get(3).test(new ItemStack(ModItems.BALANCED_SHARD.get())),
                "the hard recipe uses balanced shards");
        }
        helper.succeed();
    }

}

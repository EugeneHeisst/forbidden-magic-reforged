package dev.visfox.forbiddenreforged.test;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.RidingCropItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
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
        "emerald_fragment_is_registered",
        "riding_crop_is_registered_as_sword",
        "wand_rods_register_as_functional_cores",
        "creative_tab_is_registered_with_fork_icon",
        "creative_tab_lists_every_registered_item",
        "orichalcum_cap_is_registered"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> ItemGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[1], () -> ItemGameTests::diabolistForkResearchAndRecipe);
        FUNCTIONS.register(TESTS[2], () -> ItemGameTests::taintCoalIsRegisteredAndIsFuel);
        FUNCTIONS.register(TESTS[3], () -> ItemGameTests::emeraldFragmentIsRegistered);
        FUNCTIONS.register(TESTS[4], () -> ItemGameTests::ridingCropIsRegisteredAsSword);
        FUNCTIONS.register(TESTS[5], () -> ItemGameTests::wandRodsRegisterAsFunctionalCores);
        FUNCTIONS.register(TESTS[6], () -> ItemGameTests::creativeTabIsRegisteredWithForkIcon);
        FUNCTIONS.register(TESTS[7], () -> ItemGameTests::creativeTabListsEveryRegisteredItem);
        FUNCTIONS.register(TESTS[8], () -> ItemGameTests::orichalcumCapIsRegistered);
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
            if (holder.get() == FmItems.DEADLY_SHARDS.get("lust").get() && !dev.visfox.forbiddenreforged.FmCreativeTab.visible("lust")) {
                continue;
            }
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
}

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

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class ItemGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "skull_axe_infusion_recipe_matches_original",
        "diabolist_fork_research_and_recipe",
        "taint_coal_is_registered_and_is_fuel",
        "emerald_fragment_is_registered",
        "riding_crop_is_registered_as_sword"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> ItemGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[1], () -> ItemGameTests::diabolistForkResearchAndRecipe);
        FUNCTIONS.register(TESTS[2], () -> ItemGameTests::taintCoalIsRegisteredAndIsFuel);
        FUNCTIONS.register(TESTS[3], () -> ItemGameTests::emeraldFragmentIsRegistered);
        FUNCTIONS.register(TESTS[4], () -> ItemGameTests::ridingCropIsRegisteredAsSword);
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
}

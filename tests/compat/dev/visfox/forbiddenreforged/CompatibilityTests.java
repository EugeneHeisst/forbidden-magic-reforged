package dev.visfox.forbiddenreforged;

import dev.eugheist.tc4modern.*;
import dev.eugheist.tc4modern.api.TC4Addons;
import dev.visfox.forbiddenreforged.compat.wand.*;

import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;

import thaumcraft.api.aspects.*;
import thaumcraft.api.research.*;

@GameTestHolder("forbiddenmagic")
@PrefixGameTestTemplate(false)
public final class CompatibilityTests {
    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void allPortsLoaded(GameTestHelper h) {
        h.assertTrue(net.neoforged.fml.ModList.get().isLoaded("tc4modern"), "Host missing");
        if (!Boolean.parseBoolean(System.getProperty("forbiddenmagic.fullPack", "true"))) {
            h.succeed();
            return;
        }
        for (var id :
                java.util.List.of(
                        "tc4modern",
                        "ttinkerer",
                        "thaumicenergistics",
                        "ic2",
                        "advanced_solar_panels",
                        "emt",
                        "gravisuite"))
            h.assertTrue(net.neoforged.fml.ModList.get().isLoaded(id), "Missing port " + id);
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void aspectsReachNativeScanning(GameTestHelper h) {
        h.assertTrue(Aspect.getAspect("infernus") == FmAspects.NETHER, "Native aspect registry");
        var tags = TC4Addons.objectTag("forbiddenmagic:wrath_shard");
        h.assertTrue(tags != null && tags.getAmount(FmAspects.WRATH) == 2, "Native object tags");
        h.assertTrue(
                (Aspect.getAspect("luxuria") == null) == FmConfig.lustDisabled(),
                "no_lust config registration");
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void researchParentsAndIconsExist(GameTestHelper h) {
        var category = ResearchCategories.researchCategories.get("FORBIDDEN");
        h.assertTrue(category != null, "Forbidden tab");
        for (var r : category.research.values()) {
            if (r.icon_item != null) h.assertTrue(!r.icon_item.isEmpty(), "Empty icon " + r.key);
            for (var parents : new String[][] {r.parents, r.parentsHidden})
                if (parents != null)
                    for (var key : parents)
                        h.assertTrue(
                                ResearchCategories.getResearch(key) != null,
                                "Missing parent " + r.key + " -> " + key);
        }
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void infusionRecipesHaveRealInputs(GameTestHelper h) {
        int found = 0;
        for (var r : TC4Addons.infusionRecipes()) {
            if (!BuiltInRegistries.ITEM
                    .getKey(r.output().get())
                    .getNamespace()
                    .equals("forbiddenmagic")) continue;
            found++;
            h.assertTrue(
                    ResearchCategories.getResearch(r.research()) != null,
                    "Missing recipe research " + r.key());
            h.assertTrue(r.central().get() != Items.AIR, "Empty central " + r.key());
            for (var c : r.components())
                h.assertTrue(c.get() != Items.AIR, "Empty component " + r.key());
        }
        h.assertTrue(found >= 12, "Missing infusions " + found);
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void arcaneWorkbenchActuallyMatches(GameTestHelper h) {
        for (var r : TC4Addons.arcaneRecipes()) {
            if (!BuiltInRegistries.ITEM
                    .getKey(r.output().getItem())
                    .getNamespace()
                    .equals("forbiddenmagic")) continue;
            var grid =
                    new java.util.ArrayList<ItemStack>(
                            java.util.Collections.nCopies(9, ItemStack.EMPTY));
            for (int y = 0; y < r.height(); y++)
                for (int x = 0; x < r.width(); x++) {
                    var ingredient = r.ingredients().get(y * r.width() + x);
                    if (!ingredient.isEmpty()) {
                        var options = ingredient.getItems();
                        h.assertTrue(
                                options.length > 0,
                                "Empty ingredient " + r.key() + " at " + x + "," + y);
                        grid.set(y * 3 + x, options[0].copy());
                    }
                }
            h.assertTrue(
                    r.matches(grid, java.util.Set.of(r.research())),
                    "Native arcane mismatch " + r.key());
        }
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void crucibleActuallyMatches(GameTestHelper h) {
        for (var entry : TC4Addons.crucibleRecipes().entrySet()) {
            var r = entry.getValue();
            if (!BuiltInRegistries.ITEM
                    .getKey(r.getRecipeOutput().getItem())
                    .getNamespace()
                    .equals("forbiddenmagic")) continue;
            var items = r.catalyst.getItems();
            h.assertTrue(items.length > 0, "Empty crucible catalyst " + entry.getKey());
            h.assertTrue(
                    r.matches(r.aspects, items[0]), "Native crucible matcher " + entry.getKey());
        }
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void nativeWandStorageAndRecharge(GameTestHelper h) {
        var wand = Wands.create(WandRegistry.CAP_IRON, WandRegistry.findRod("infernal"), false);
        h.assertTrue(
                WandData.isWand(wand) && WandData.capacity(wand) == 15000, "Native 150 vis wand");
        Wands.addVis(wand, Aspect.FIRE, 1, true);
        h.assertTrue(WandData.get(wand, Aspect.FIRE) == 100, "Whole vis conversion");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.tickCount = 100;
        WandRegistry.UPDATES.get("infernal").onUpdate(wand, player);
        h.assertTrue(WandData.get(wand, Aspect.FIRE) == 200, "Infernal recharge");
        h.assertTrue(
                !WandData.consumeCrafting(
                        wand, new AspectList().add(Aspect.FIRE, 201), false, player),
                "Prevent free vis");
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void nativeEnchantmentAndTinkererRegistries(GameTestHelper h) {
        if (!FmConfig.enchantmentsEnabled()) {
            h.assertTrue(
                    dev.visfox.forbiddenreforged.compat.crafting.TcRecipes.ENCHANTMENTS.isEmpty()
                            && FmTinkerer.registeredCount() == 0,
                    "Disabled enchantments registered");
            h.succeed();
            return;
        }
        var all =
                thaumcraft.common.tiles.infusion.InfusionEnchantments.all(
                        () -> InfusionContent.SALIS.get());
        for (var key :
                java.util.List.of(
                        FmEnchantments.WRATH,
                        FmEnchantments.CONSUMING,
                        FmEnchantments.EDUCATIONAL,
                        FmEnchantments.IMPACT,
                        FmEnchantments.VOIDTOUCHED))
            h.assertTrue(
                    all.stream().anyMatch(r -> r.enchantment().equals(key)),
                    "Missing native infusion enchant " + key);
        h.assertTrue(
                FmTinkerer.registeredCount()
                        == (net.neoforged.fml.ModList.get().isLoaded("ttinkerer")
                                ? (FmConfig.greedyEnchantmentEnabled() ? 8 : 7)
                                : 0),
                "Tinkerer costs not registered");
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void cageAcceptsNativeTubeAndPersists(GameTestHelper h) {
        h.setBlock(new BlockPos(1, 1, 1), FmBlocks.WRATH_CAGE.get());
        var cage = (WrathCageBlockEntity) h.getBlockEntity(new BlockPos(1, 1, 1));
        cage.setMobId("minecraft:zombie");
        h.assertTrue(cage instanceof IEssentiaTransport, "Native essentia interface");
        h.assertTrue(
                cage.addEssentia(FmAspects.WRATH, 5, Direction.UP) == 5, "Native tube injection");
        var saved = cage.saveWithoutMetadata(h.getLevel().registryAccess());
        var copy = new WrathCageBlockEntity(cage.getBlockPos(), cage.getBlockState());
        copy.loadWithComponents(saved, h.getLevel().registryAccess());
        h.assertTrue(
                copy.containerContains(FmAspects.WRATH) == 5
                        && "minecraft:zombie".equals(copy.getMobId()),
                "Cage NBT persistence");
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void baublesAndScribesUseNativeInterfaces(GameTestHelper h) {
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var ring = new ItemStack(FmItems.RING_NUTRITION.get());
        h.assertTrue(
                BaublesInventory.fits(1, ring, player) && !BaublesInventory.fits(0, ring, player),
                "Ring slots");
        for (var item : java.util.List.of(FmItems.CRYSTALWELL.get(), FmItems.PRIMEWELL.get())) {
            var stack = new ItemStack(item);
            h.assertTrue(
                    ScribingToolsItem.hasInk(stack) && stack.isDamageableItem(),
                    "Native scribe durability " + stack);
        }
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void focusUpgradesUseNativeManipulator(GameTestHelper h) {
        var focus = new ItemStack(FmItems.BLINK_FOCUS.get());
        var item = (BlinkFocusItem) focus.getItem();
        h.assertTrue(
                FociContent.upgradeType(100) != null && FociContent.upgradeType(101) != null,
                "Custom upgrade registration");
        h.assertTrue(item.applyUpgrade(focus, 100, 2), "Native upgrade application");
        h.assertTrue(item.getVisCost(focus).getAmount(Aspect.FIRE) == 100, "Hellfire vis cost");
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void enchantmentInfusionRequiresOwnResearch(GameTestHelper h) {
        if (!FmConfig.enchantmentsEnabled()) {
            h.succeed();
            return;
        }
        h.setBlock(new BlockPos(1, 1, 1), InfusionContent.MATRIX.get());
        var matrix = (InfusionContent.MatrixEntity) h.getBlockEntity(new BlockPos(1, 1, 1));
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ResearchProgress.add(player.getPersistentData(), "INFUSIONENCHANTMENT");
        var recipe =
                dev.visfox.forbiddenreforged.compat.crafting.TcRecipes.ENCHANTMENTS.stream()
                        .filter(r -> r.enchantment().equals(FmEnchantments.WRATH))
                        .findFirst()
                        .orElseThrow();
        var components = recipe.components().stream().map(c -> new ItemStack(c.get())).toList();
        var central = new ItemStack(Items.DIAMOND_SWORD);
        h.assertTrue(
                matrix.findRecipe(components, central, player) == null,
                "Unresearched FM enchant accepted");
        ResearchProgress.add(player.getPersistentData(), "WRATH");
        h.assertTrue(
                matrix.findRecipe(components, central, player) != null,
                "Researched FM enchant rejected");
        h.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void tinkererButtonsBeyondFortyWork(GameTestHelper h) {
        if (!FmConfig.enchantmentsEnabled()
                || !net.neoforged.fml.ModList.get().isLoaded("ttinkerer")) {
            h.succeed();
            return;
        }
        try {
            var type = Class.forName("dev.eugheist.ttmodern.EnchanterEntity");
            var menuType = Class.forName("dev.eugheist.ttmodern.EnchanterMenu");
            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("ttinkerer:enchanter"));
            h.setBlock(new BlockPos(1, 1, 1), block);
            var tile = h.getBlockEntity(new BlockPos(1, 1, 1));
            var inventory =
                    (net.neoforged.neoforge.items.IItemHandlerModifiable)
                            type.getField("inventory").get(tile);
            inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND_SWORD));
            var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            player.setPos(tile.getBlockPos().getCenter());
            ResearchProgress.add(player.getPersistentData(), "WRATH");
            var menu =
                    (net.minecraft.world.inventory.AbstractContainerMenu)
                            menuType.getConstructor(
                                            int.class,
                                            net.minecraft.world.entity.player.Inventory.class,
                                            type)
                                    .newInstance(1, player.getInventory(), tile);
            var idsField = menuType.getDeclaredField("IDS");
            idsField.setAccessible(true);
            var ids = (java.util.List<?>) idsField.get(null);
            for (var key :
                    java.util.List.of(
                            FmEnchantments.WRATH,
                            FmEnchantments.GREEDY,
                            FmEnchantments.CONSUMING,
                            FmEnchantments.EDUCATIONAL,
                            FmEnchantments.CORRUPTING,
                            FmEnchantments.CLUSTER,
                            FmEnchantments.IMPACT,
                            FmEnchantments.VOIDTOUCHED)) {
                if (key.equals(FmEnchantments.GREEDY) && !FmConfig.greedyEnchantmentEnabled())
                    continue;
                var enchant =
                        h.getLevel()
                                .registryAccess()
                                .lookupOrThrow(Registries.ENCHANTMENT)
                                .getOrThrow(key);
                var candidates =
                        java.util.List.of(
                                FmItems.MORPH_PICKAXE.get(),
                                FmItems.MORPH_SWORD.get(),
                                Items.DIAMOND_PICKAXE,
                                Items.DIAMOND_SWORD,
                                Items.BOW);
                var tool =
                        candidates.stream()
                                .map(ItemStack::new)
                                .filter(
                                        stack ->
                                                stack.isEnchantable()
                                                        && enchant.value().canEnchant(stack))
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "No valid enchantable tool " + key));
                inventory.setStackInSlot(0, tool);
                String research = key.location().getPath().toUpperCase(java.util.Locale.ROOT);
                ResearchProgress.add(player.getPersistentData(), research);
                int index = ids.indexOf(key.location().toString());
                h.assertTrue(index >= 40, "FM enchant not appended " + key);
                h.assertTrue(
                        menu.clickMenuButton(player, 1 + index * 3),
                        "FM enchant menu button rejected " + key);
                h.assertTrue(
                        (int) menuType.getMethod("rank", int.class).invoke(menu, index) == 1,
                        "FM enchant rank not synced " + key);
            }
            h.succeed();
        } catch (ReflectiveOperationException error) {
            throw new RuntimeException(error);
        }
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void cagePullsFromNativeJarWithoutOverflow(GameTestHelper h) {
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, FmBlocks.WRATH_CAGE.get());
        h.setBlock(
                pos.below(),
                BuiltInRegistries.BLOCK.get(ResourceLocation.parse("tc4modern:warded_jar")));
        var cage = (WrathCageBlockEntity) h.getBlockEntity(pos);
        cage.setMobId("minecraft:zombie");
        var jar = (dev.eugheist.tc4modern.WardedJarEntity) h.getBlockEntity(pos.below());
        jar.state.aspect = FmAspects.WRATH;
        jar.state.amount = 10;
        WrathCageBlockEntity.tick(h.getLevel(), cage.getBlockPos(), cage.getBlockState(), cage);
        h.assertTrue(
                cage.containerContains(FmAspects.WRATH) == 1 && jar.state.amount == 9,
                "Native jar-to-cage transfer");
        h.assertTrue(
                cage.addEssentia(FmAspects.WRATH, 100, Direction.UP) == 63, "Overflow accepted");
        h.assertTrue(cage.containerContains(FmAspects.WRATH) == 64, "Capacity mismatch");
        h.assertTrue(cage.addEssentia(Aspect.AIR, 5, Direction.UP) == 0, "Wrong aspect accepted");
        h.assertTrue(
                dev.eugheist.tc4modern.api.AspectContainers.find(cage) != null,
                "Energistics container bridge missing");
        h.succeed();
    }
}

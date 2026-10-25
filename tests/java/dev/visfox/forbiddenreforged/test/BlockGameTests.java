package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.ArcaneCakeBlock;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.TaintTree;
import dev.visfox.forbiddenreforged.TaintLogTextureVariant;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.taint.TaintSpread;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class BlockGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "taint_material_blocks_are_registered",
        "taint_blocks_carry_tool_tags",
        "taint_materials_resist_fibrous_taint",
        "blocks_drop_themselves",
        "taint_sapling_grows_a_tree",
        "umbral_plants_are_registered",
        "arcane_cake_feeds_and_keeps_slices",
        "taint_log_selects_side_textures_deterministically"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> BlockGameTests::taintMaterialBlocksAreRegistered);
        FUNCTIONS.register(TESTS[1], () -> BlockGameTests::taintBlocksCarryToolTags);
        FUNCTIONS.register(TESTS[2], () -> BlockGameTests::taintMaterialsResistFibrousTaint);
        FUNCTIONS.register(TESTS[3], () -> BlockGameTests::blocksDropThemselves);
        FUNCTIONS.register(TESTS[3], () -> BlockGameTests::taintSaplingGrowsATree);
        FUNCTIONS.register(TESTS[4], () -> BlockGameTests::umbralPlantsAreRegistered);
        FUNCTIONS.register(TESTS[5], () -> BlockGameTests::arcaneCakeFeedsAndKeepsSlices);
        FUNCTIONS.register(TESTS[6], () -> BlockGameTests::taintLogSelectsSideTexturesDeterministically);
    }

    private BlockGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_blocks"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
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

    private static void taintMaterialBlocksAreRegistered(GameTestHelper helper) {
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_LOG.get()), Identifier.parse("forbiddenmagic:taint_log"), "taint log id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_PLANKS.get()), Identifier.parse("forbiddenmagic:taint_planks"), "taint planks id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_SMOOTH.get()), Identifier.parse("forbiddenmagic:taint_stone_smooth"), "smooth stone id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_BRICK.get()), Identifier.parse("forbiddenmagic:taint_stone_brick"), "brick id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.STAR_BLOCK.get()), Identifier.parse("forbiddenmagic:star_block"), "star block id");
        helper.assertFalse(new ItemStack(FmBlocks.TAINT_LOG.get().asItem()).isEmpty(), "taint log has an item");
        helper.assertFalse(new ItemStack(FmBlocks.STAR_BLOCK.get().asItem()).isEmpty(), "star block has an item");
        helper.succeed();
    }

    private static void taintBlocksCarryToolTags(GameTestHelper helper) {
        TagKey<Block> axe = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:mineable/axe"));
        TagKey<Block> pickaxe = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:mineable/pickaxe"));
        TagKey<Block> beacon = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:beacon_base_blocks"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_LOG.get()).is(axe), "taint log mines with an axe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_PLANKS.get()).is(axe), "taint planks mine with an axe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_STONE_SMOOTH.get()).is(pickaxe), "taint stone mines with a pickaxe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.STAR_BLOCK.get()).is(beacon), "star block is a beacon base");
        helper.succeed();
    }

    private static void taintMaterialsResistFibrousTaint(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        for (Block block : List.of(
            FmBlocks.TAINT_LOG.get(),
            FmBlocks.TAINT_PLANKS.get(),
            FmBlocks.TAINT_STONE_SMOOTH.get(),
            FmBlocks.TAINT_STONE_BRICK.get()
        )) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            helper.assertFalse(TaintSpread.spreadFibres(level, pos),
                BuiltInRegistries.BLOCK.getKey(block) + " must not be replaced by fibrous taint");
            helper.assertTrue(level.getBlockState(pos).is(block),
                BuiltInRegistries.BLOCK.getKey(block) + " remains intact after a spread attempt");
        }

        helper.assertFalse(FmBlocks.TAINT_LOG.get().defaultBlockState().is(BlockTags.LOGS),
            "taint log must not enter the host's vanilla-log crusting path");
        helper.assertFalse(FmBlocks.TAINT_PLANKS.get().defaultBlockState().is(BlockTags.LOGS),
            "taint planks must not enter the host's vanilla-log crusting path");
        for (Block block : List.of(FmBlocks.TAINT_PLANKS.get(), FmBlocks.TAINT_STONE_SMOOTH.get(), FmBlocks.TAINT_STONE_BRICK.get())) {
            var state = block.defaultBlockState();
            helper.assertFalse(state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
                || state.is(Blocks.CLAY) || state.is(Blocks.GRASS_BLOCK),
                BuiltInRegistries.BLOCK.getKey(block) + " must not enter the host's soil conversion path");
        }
        helper.succeed();
    }

    private static void blocksDropThemselves(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(0, 4, 0);
        for (var block : List.of(
            FmBlocks.TAINT_LOG.get(), FmBlocks.TAINT_PLANKS.get(),
            FmBlocks.TAINT_STONE_SMOOTH.get(), FmBlocks.TAINT_STONE_BRICK.get(),
            FmBlocks.STAR_BLOCK.get())) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            var drops = Block.getDrops(level.getBlockState(pos), level, pos, null);
            helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(block.asItem())), BuiltInRegistries.BLOCK.getKey(block) + " must drop itself");
        }
        helper.succeed();
    }

    private static void taintSaplingGrowsATree(GameTestHelper helper) {
        BlockPos soil = new BlockPos(2, 4, 2);
        helper.getLevel().setBlockAndUpdate(soil, Blocks.DIRT.defaultBlockState());
        BlockPos sapling = soil.above();
        helper.getLevel().setBlockAndUpdate(sapling, FmBlocks.TAINT_SAPLING.get().defaultBlockState());
        boolean grown = TaintTree.grow(helper.getLevel(), sapling, RandomSource.create(3L));
        helper.assertTrue(grown, "tree must grow on open ground");
        helper.assertTrue(helper.getLevel().getBlockState(sapling).is(FmBlocks.TAINT_LOG.get()), "trunk replaces the sapling");
        boolean canopy = false;
        for (BlockPos cursor : BlockPos.betweenClosed(sapling.above(2).offset(-2, 0, -2), sapling.above(7).offset(2, 0, 2))) {
            if (helper.getLevel().getBlockState(cursor).is(FmBlocks.TAINT_LEAVES.get())) {
                canopy = true;
                break;
            }
        }
        helper.assertTrue(canopy, "canopy sits above the trunk");
        helper.succeed();
    }

    private static void umbralPlantsAreRegistered(GameTestHelper helper) {
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.UMBRAL_ROSE.get()), Identifier.parse("forbiddenmagic:umbral_rose"), "umbral rose id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.UMBRAL_ROSE_BUSH.get()), Identifier.parse("forbiddenmagic:umbral_rose_bush"), "umbral rose bush id");
        TagKey<Block> flowers = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:small_flowers"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.UMBRAL_ROSE.get()).is(flowers), "umbral rose is a small flower");
        helper.succeed();
    }

    private static void arcaneCakeFeedsAndKeepsSlices(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(4, 4, 4);
        level.setBlockAndUpdate(pos, FmBlocks.ARCANE_CAKE.get().defaultBlockState());
        helper.assertTrue(ResearchCategories.getResearch("ARCANECAKE") != null, "cake research exists");
        helper.assertTrue(TcRecipes.get("ARCANECAKE") instanceof InfusionRecipe, "cake recipe exists");
        ServerPlayer player = mockPlayer(helper);
        player.getFoodData().setFoodLevel(10);
        ArcaneCakeBlock.eat(level, pos, level.getBlockState(pos), player);
        helper.assertValueEqual(level.getBlockState(pos).getValue(ArcaneCakeBlock.SLICE), 1, "one slice eaten");
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 12, "cake restores two hunger");
        helper.succeed();
    }

    private static void taintLogSelectsSideTexturesDeterministically(GameTestHelper helper) {
        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/blockstates/taint_log.json")) {
            helper.assertTrue(stream != null, "taint log blockstate must ship with the mod");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String axis : List.of("axis=x", "axis=y", "axis=z")) {
                JsonObject definition = root.getAsJsonObject("variants").getAsJsonObject(axis);
                helper.assertValueEqual(definition.get("type").getAsString(), "forbiddenmagic:taint_log_faces",
                    axis + " must use the coordinate-aware face model");
            }

            int[] counts = new int[3];
            for (int x = -16; x <= 16; x++) {
                for (int z = -16; z <= 16; z++) {
                    for (int side = 0; side < 6; side++) {
                        int selected = TaintLogTextureVariant.select(side, x, 7, z);
                        helper.assertTrue(selected >= 0 && selected <= 2, "face texture selector must return a known variant");
                        helper.assertValueEqual(selected, TaintLogTextureVariant.select(side, x, 7, z),
                            "face texture selection must be stable for a given side and position");
                        counts[selected]++;
                    }
                }
            }
            for (int variant = 0; variant < counts.length; variant++) {
                helper.assertTrue(counts[variant] > 0, "coordinate sample must exercise side-texture variant " + variant);
            }

            helper.assertValueEqual(TaintLogTextureVariant.select(3, 3, 7, 5), TaintLogTextureVariant.GROSS,
                "selection must use the original side + y + x*z seed");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(net.minecraft.core.Direction.Axis.Y,
                net.minecraft.core.Direction.DOWN, 3, 7, 5), TaintLogTextureVariant.STANDARD,
                "vertical log ends keep the end texture");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(net.minecraft.core.Direction.Axis.Y,
                net.minecraft.core.Direction.SOUTH, 3, 7, 5), TaintLogTextureVariant.GROSS,
                "vertical log sides use the coordinate-selected variant");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(net.minecraft.core.Direction.Axis.X,
                net.minecraft.core.Direction.EAST, 3, 7, 5), TaintLogTextureVariant.STANDARD,
                "horizontal log ends keep the end texture");
            helper.succeed();
        } catch (IOException e) {
            helper.fail("could not read the taint log blockstate: " + e);
        }
    }
}

package dev.visfox.forbiddenreforged.test;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);
    private static final String[] TESTS = {"mod_loads_and_host_is_present", "aspect_registry_keeps_builtin_order_and_combines", "addon_aspect_survives_client_sync", "sin_aspects_registered_with_own_textures"};

    static {
        FUNCTIONS.register(TESTS[0], () -> FmGameTests::modLoadsAndHostIsPresent);
        FUNCTIONS.register(TESTS[1], () -> FmGameTests::aspectRegistryKeepsBuiltinOrderAndCombines);
        FUNCTIONS.register(TESTS[2], () -> FmGameTests::addonAspectSurvivesClientSync);
        FUNCTIONS.register(TESTS[3], () -> FmGameTests::sinAspectsRegisteredWithOwnTextures);
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
}

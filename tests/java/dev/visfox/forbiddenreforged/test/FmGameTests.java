package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;

@GameTestHolder(ForbiddenMagic.MODID)
public final class FmGameTests {
    private FmGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "minecraft")
    public static void modLoadsAndHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "thaumcraft must be loaded");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "minecraft")
    public static void itemRegistryContainsPortedBaseItems(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("taint_fruit")) != net.minecraft.world.item.Items.AIR,
            "taint fruit must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("black_dye")) != net.minecraft.world.item.Items.AIR,
            "black dye must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("lust_shard")) != net.minecraft.world.item.Items.AIR,
            "lust shard must be registered");
        helper.succeed();
    }


    @GameTest(template = "empty", templateNamespace = "minecraft")
    public static void itemAspectTagsExist(GameTestHelper helper) {
        AspectList fruit = AspectRegistry.active().getObjectTags(FmItems.TAINT_FRUIT.get());
        AspectList shard = AspectRegistry.active().getObjectTags(FmItems.DEADLY_SHARDS.get("wrath").get());
        helper.assertTrue(fruit != null && fruit.getAmount(Aspect.TAINT) >= 2,
            "taint fruit must carry taint aspects");
        helper.assertTrue(shard != null && shard.getAmount(FmAspects.WRATH) >= 2,
            "wrath shard must carry wrath aspects");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "minecraft")
    public static void aspectRegistryKeepsBuiltinOrderAndCombines(GameTestHelper helper) {
        Aspect[] all = Aspect.values();
        helper.assertValueEqual(Aspect.builtinCount(), 48, "exactly 48 builtin aspects");
        helper.assertTrue(all.length >= 48, "builtins must remain registered");
        helper.assertValueEqual(all[0], Aspect.AIR, "first builtin must stay index 0");
        helper.assertValueEqual(all[47], Aspect.TRAP, "last builtin must stay index 47");
        helper.assertTrue(Aspect.combine(Aspect.AIR, Aspect.FIRE) == Aspect.LIGHT, "combine(AIR, FIRE) must be LIGHT");
        helper.assertTrue(Aspect.getAspect("aer") == Aspect.AIR, "by-tag lookup must work");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "minecraft")
    public static void addonAspectsSurviveSerialization(GameTestHelper helper) {
        Aspect nether = FmAspects.NETHER;
        helper.assertTrue(nether != null && nether.ordinal() >= Aspect.builtinCount(), "infernus must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("infernus"), nether, "by-tag lookup must find infernus");
        helper.assertTrue(FmAspects.PRIDE != null && FmAspects.GLUTTONY != null && FmAspects.ENVY != null
            && FmAspects.SLOTH != null && FmAspects.WRATH != null, "all enabled sin aspects must be registered");
        if (!FmConfig.lustDisabled()) {
            helper.assertTrue(FmAspects.LUST != null, "luxuria must register when no_lust is disabled");
        } else {
            helper.assertTrue(Aspect.getAspect("luxuria") == null, "luxuria must not register when no_lust is enabled");
        }
        AspectList list = new AspectList().add(nether, 3);
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(nether), 3, "aspect amount must survive a network round trip");
        buffer.release();
        helper.succeed();
    }
}

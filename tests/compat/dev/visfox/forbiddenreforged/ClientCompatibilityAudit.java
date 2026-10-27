package dev.visfox.forbiddenreforged;

import dev.eugheist.tc4modern.*;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@EventBusSubscriber(modid = "forbiddenmagic", value = Dist.CLIENT)
public final class ClientCompatibilityAudit {
    private static int ticks, stage, frames, models;
    private static CompletableFuture<Void> work;
    private static String shot;
    private static final BlockPos POS = new BlockPos(300, 180, 300);
    private static final Set<String> seen = new HashSet<>();
    private static List<ItemStack> stacks;

    private static void check(boolean good, String message) {
        if (!good) throw new IllegalStateException("FM CLIENT FAIL " + message);
        System.out.println("FM CLIENT PASS " + message);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) throws Exception {
        if (!Boolean.getBoolean("forbiddenmagic.clientAudit")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null
                || mc.player == null
                || mc.getSingleplayerServer() == null
                || shot != null) return;
        if (++ticks > 1800) throw new IllegalStateException("FM CLIENT timeout");
        if (stage == 0) {
            stacks = FmCreativeTab.stacks();
            for (var s : stacks) {
                var model = mc.getItemRenderer().getModel(s, mc.level, mc.player, 0);
                check(
                        !model.getParticleIcon(
                                        net.neoforged.neoforge.client.model.data.ModelData.EMPTY)
                                .contents()
                                .name()
                                .toString()
                                .equals("minecraft:missingno"),
                        "baked item " + BuiltInRegistries.ITEM.getKey(s.getItem()));
                models++;
            }
            mc.setScreen(new Gallery());
            shot = "01-items-and-wands";
            stage = 1;
            return;
        }
        if (stage == 1) {
            var id = mc.player.getUUID();
            work =
                    mc.getSingleplayerServer()
                            .submit(
                                    () -> {
                                        var p =
                                                mc.getSingleplayerServer()
                                                        .getPlayerList()
                                                        .getPlayer(id);
                                        for (var category :
                                                thaumcraft.api.research.ResearchCategories
                                                        .researchCategories
                                                        .values())
                                            for (var research : category.research.values())
                                                ResearchProgress.add(
                                                        p.getPersistentData(), research.key);
                                        dev.eugheist.tc4modern.ThaumometerItem.sync(p, "fm_audit");
                                        for (int x = -3; x <= 3; x++)
                                            for (int z = -3; z <= 3; z++)
                                                p.serverLevel()
                                                        .setBlockAndUpdate(
                                                                POS.offset(x, -1, z),
                                                                net.minecraft.world.level.block
                                                                        .Blocks.STONE
                                                                        .defaultBlockState());
                                        var block =
                                                BuiltInRegistries.BLOCK.get(
                                                        ResourceLocation.parse(
                                                                "ttinkerer:enchanter"));
                                        p.serverLevel()
                                                .setBlockAndUpdate(POS, block.defaultBlockState());
                                        p.connection.teleport(
                                                POS.getX() + 2, POS.getY(), POS.getZ() + .5, 90, 0);
                                    })
                            .thenApply(value -> null);
            stage = 2;
            return;
        }
        if (stage == 2) {
            if (!work.isDone() || mc.level.getBlockEntity(POS) == null) return;
            work.join();
            open(mc, new ItemStack(FmItems.MORPH_SWORD.get()));
            stage = 3;
            ticks = 0;
            return;
        }
        if (stage == 3 || stage == 5) {
            if (!work.isDone()) return;
            work.join();
            if (mc.screen == null
                    || !mc.screen
                            .getClass()
                            .getName()
                            .equals("dev.eugheist.ttmodern.EnchanterScreen")) return;
            if (ticks < 30) return;
            scanPages(mc, stage == 3 ? "sword" : "pickaxe");
            shot = stage == 3 ? "02-enchanter-sword" : "03-enchanter-pickaxe";
            stage++;
            return;
        }
        if (stage == 4) {
            open(mc, new ItemStack(FmItems.MORPH_PICKAXE.get()));
            stage = 5;
            ticks = 0;
            return;
        }
        if (stage == 6) {
            check(
                    seen.size() == 8,
                    "all eight FM enchantments visible in native Tinkerer screen: " + seen);
            System.out.println("FM CLIENT DONE models=" + models + " enchantments=" + seen.size());
            Files.writeString(
                    mc.gameDirectory.toPath().resolve("CLIENT-PASS.txt"),
                    "models=" + models + "\nenchantments=" + seen + "\n");
            mc.stop();
            stage = 7;
        }
    }

    private static void open(Minecraft mc, ItemStack tool) {
        var id = mc.player.getUUID();
        work =
                mc.getSingleplayerServer()
                        .submit(
                                () -> {
                                    var p =
                                            mc.getSingleplayerServer()
                                                    .getPlayerList()
                                                    .getPlayer(id);
                                    p.closeContainer();
                                    var level = p.serverLevel();
                                    var state = level.getBlockState(POS);
                                    var tile = level.getBlockEntity(POS);
                                    try {
                                        var inventory =
                                                (net.neoforged.neoforge.items
                                                                .IItemHandlerModifiable)
                                                        tile.getClass()
                                                                .getField("inventory")
                                                                .get(tile);
                                        inventory.setStackInSlot(0, tool);
                                        state.useWithoutItem(
                                                level,
                                                p,
                                                new net.minecraft.world.phys.BlockHitResult(
                                                        POS.getCenter(), Direction.UP, POS, false));
                                    } catch (Exception error) {
                                        throw new RuntimeException(error);
                                    }
                                })
                        .thenApply(value -> null);
    }

    private static void scanPages(Minecraft mc, String prefix) throws Exception {
        var screen = mc.screen;
        var method = screen.getClass().getDeclaredMethod("available");
        method.setAccessible(true);
        var menuClass = Class.forName("dev.eugheist.ttmodern.EnchanterMenu");
        var field = menuClass.getDeclaredField("IDS");
        field.setAccessible(true);
        var ids = (List<?>) field.get(null);
        for (int page = 0; page < 4; page++) {
            var available = (List<?>) method.invoke(screen);
            for (var index : available) {
                String id = ids.get((Integer) index).toString();
                if (id.startsWith("forbiddenmagic:")) seen.add(id);
            }
            screen.mouseScrolled(0, 0, 0, -1);
        }
    }

    private static void save(Minecraft mc, String name) throws Exception {
        var dir = mc.gameDirectory.toPath().resolve("proofs");
        Files.createDirectories(dir);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(dir.resolve(name + ".png"));
        }
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Post e) throws Exception {
        if (shot == null || ++frames < 5) return;
        save(Minecraft.getInstance(), shot);
        shot = null;
        frames = 0;
    }

    private static final class Gallery extends Screen {
        Gallery() {
            super(Component.literal("Forbidden Magic — 1.21.1 / TC4Modern"));
        }

        @Override
        public void renderBackground(GuiGraphics g, int x, int y, float partial) {}

        @Override
        public void render(GuiGraphics g, int x, int y, float partial) {
            g.fill(0, 0, width, height, 0xFF202028);
            g.drawString(font, title, 12, 10, 0xFFFFFF);
            int col = 0, row = 0;
            for (var stack : stacks) {
                g.renderItem(stack, 14 + col * 25, 34 + row * 26);
                if (++col == 18) {
                    col = 0;
                    row++;
                }
            }
            super.render(g, x, y, partial);
        }
    }
}

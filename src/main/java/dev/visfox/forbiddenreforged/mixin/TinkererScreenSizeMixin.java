package dev.visfox.forbiddenreforged.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Pseudo
@Mixin(targets = "dev.eugheist.ttmodern.EnchanterScreen", remap = false)
public abstract class TinkererScreenSizeMixin extends Screen {
    @Unique private int forbiddenmagic$page;
    @Unique private int forbiddenmagic$pages = 1;

    protected TinkererScreenSizeMixin(Component title) {
        super(title);
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 40), require = 1)
    private int count(int original) {
        return dev.visfox.forbiddenreforged.FmTinkerer.totalEntries;
    }

    @ModifyConstant(method = "available", constant = @Constant(intValue = 16), require = 1)
    private int allChoices(int original) {
        return Integer.MAX_VALUE;
    }

    @Inject(method = "available", at = @At("RETURN"), cancellable = true)
    private void page(CallbackInfoReturnable<List<Integer>> cir) {
        var all = cir.getReturnValue();
        forbiddenmagic$pages = Math.max(1, (all.size() + 15) / 16);
        forbiddenmagic$page = Math.floorMod(forbiddenmagic$page, forbiddenmagic$pages);
        int from = forbiddenmagic$page * 16;
        cir.setReturnValue(new ArrayList<>(all.subList(from, Math.min(from + 16, all.size()))));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (forbiddenmagic$pages <= 1 || vertical == 0)
            return super.mouseScrolled(x, y, horizontal, vertical);
        forbiddenmagic$page =
                Math.floorMod(forbiddenmagic$page + (vertical > 0 ? -1 : 1), forbiddenmagic$pages);
        return true;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void pageHint(
            net.minecraft.client.gui.GuiGraphics g,
            int x,
            int y,
            float partial,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (forbiddenmagic$pages > 1)
            g.drawString(
                    font,
                    Component.translatable(
                            "forbiddenmagic.enchanter.page",
                            forbiddenmagic$page + 1,
                            forbiddenmagic$pages),
                    (width - 176) / 2,
                    (height - 166) / 2 - 12,
                    0xFFFFFF,
                    true);
    }
}

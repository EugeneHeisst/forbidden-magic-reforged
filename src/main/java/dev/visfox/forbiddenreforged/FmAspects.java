package dev.visfox.forbiddenreforged;

import javax.annotation.Nullable;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;

public final class FmAspects {
    public static @Nullable Aspect PROBE;

    private FmAspects() {
    }

    public static boolean available() {
        return AddonAspectsAvailable.check();
    }

    public static void init() {
        AddonAspects.addRegistrar(api -> PROBE = api.register("probe", 0x808080, new Aspect[]{Aspect.AIR, Aspect.VOID}, 1, null));
        AddonAspects.init();
    }

    private static final class AddonAspectsAvailable {
        static boolean check() {
            try {
                Class.forName("thaumcraft.aspect.AddonAspects");
                return true;
            } catch (ClassNotFoundException absent) {
                return false;
            }
        }
    }
}

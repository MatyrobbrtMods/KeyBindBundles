package com.matyrobbrt.keybindbundles;

import net.minecraftforge.common.ForgeConfigSpec;

public class KBClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue IGNORE_INVALID_KEY_CHECKS;
    static {
        var builder = new ForgeConfigSpec.Builder();
        IGNORE_INVALID_KEY_CHECKS = builder
                .comment("ONLY USE THIS IF YOU KNOW WHAT YOU'RE DOING")
                .comment("Ignore invalid key checks in InputConstants#isKeyDown")
                .define("ignoreInvalidKeyChecks", false);
        SPEC = builder.build();
    }
}

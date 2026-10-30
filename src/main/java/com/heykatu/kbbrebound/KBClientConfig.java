package com.heykatu.kbbrebound;

import net.minecraftforge.common.ForgeConfigSpec;

public class KBClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue IGNORE_INVALID_KEY_CHECKS;
    public static final ForgeConfigSpec.BooleanValue TOGGLE_MENU;
    static {
        var builder = new ForgeConfigSpec.Builder();
        TOGGLE_MENU = builder
                .comment("If true, pressing a bundle key opens its menu until you press it again, pick an entry or open a screen. If false, the menu is only open while the key is held")
                .define("toggleMenu", true);
        IGNORE_INVALID_KEY_CHECKS = builder
                .comment("ONLY USE THIS IF YOU KNOW WHAT YOU'RE DOING")
                .comment("Ignore invalid key checks in InputConstants#isKeyDown")
                .define("ignoreInvalidKeyChecks", false);
        SPEC = builder.build();
    }
}

package com.heykatu.kbbrebound;

import net.neoforged.neoforge.common.ModConfigSpec;

public class KBClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue CLIP_MOUSE_TO_MENU;
    public static final ModConfigSpec.BooleanValue TRIGGER_KEYMAPPING_ON_RELEASE;
    public static final ModConfigSpec.BooleanValue IGNORE_INVALID_KEY_CHECKS;
    public static final ModConfigSpec.BooleanValue TOGGLE_MENU;
    static {
        var builder = new ModConfigSpec.Builder();
        TOGGLE_MENU = builder
                .comment("If true, pressing a bundle key opens its menu until you press it again, pick an entry or open a screen. If false, the menu is only open while the key is held")
                .define("toggleMenu", true);
        CLIP_MOUSE_TO_MENU = builder
                .comment("Set to true to clip the mouse within the bounds of the radial menu of bundles")
                .define("clipMouseToMenu", false);
        TRIGGER_KEYMAPPING_ON_RELEASE = builder
                .comment("If set to true, the keymapping hovered in a bundle menu will be automatically triggered (without needing a click) upon release of the bundle key")
                .define("triggerKeymappingOnRelease", false);
        IGNORE_INVALID_KEY_CHECKS = builder
                .comment("ONLY USE THIS IF YOU KNOW WHAT YOU'RE DOING")
                .comment("Ignore invalid key checks in InputConstants#isKeyDown")
                .define("ignoreInvalidKeyChecks", false);
        SPEC = builder.build();
    }
}

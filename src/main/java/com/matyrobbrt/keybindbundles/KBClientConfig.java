package com.matyrobbrt.keybindbundles;

import net.neoforged.neoforge.common.ModConfigSpec;

public class KBClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue CLIP_MOUSE_TO_MENU;
    public static final ModConfigSpec.BooleanValue TRIGGER_KEYMAPPING_ON_RELEASE;
    public static final ModConfigSpec.BooleanValue IGNORE_INVALID_KEY_CHECKS;
    static {
        var builder = new ModConfigSpec.Builder();
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

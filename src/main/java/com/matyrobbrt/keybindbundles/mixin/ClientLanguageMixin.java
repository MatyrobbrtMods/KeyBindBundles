package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// bundles get made in game so they cant have real lang entries, fake them so other mods (keybind atlas etc) see the name
@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
    @Unique
    private static final String KBB_BUNDLE_PREFIX = "key.keybindbundles.bundle_";

    @Inject(at = @At("HEAD"), method = "getOrDefault", cancellable = true)
    private void bundleName(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        if (!key.startsWith(KBB_BUNDLE_PREFIX)) return;
        var name = KeyBindBundleManager.getBundleName(key);
        if (name == null) return;

        var format = ((Language) (Object) this).getOrDefault("key.keybindbundles.bundle", "Key bundle %s");
        // no gold here, plain text plays nicer with other mods
        cir.setReturnValue(ChatFormatting.stripFormatting(String.format(format, name)));
    }

    @Inject(at = @At("HEAD"), method = "has", cancellable = true)
    private void hasBundleName(String key, CallbackInfoReturnable<Boolean> cir) {
        if (key.startsWith(KBB_BUNDLE_PREFIX) && KeyBindBundleManager.getBundleName(key) != null) {
            cir.setReturnValue(true);
        }
    }
}

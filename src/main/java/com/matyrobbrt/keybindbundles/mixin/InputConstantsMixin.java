package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KBClientConfig;
import com.matyrobbrt.keybindbundles.ModKeyBindBundlesClient;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Mixin(value = InputConstants.class, priority = 1500)
public class InputConstantsMixin {
    @Unique
    private static final Set<Class<?>> IGNORED_INVALID_CALLSITES = new CopyOnWriteArraySet<>();

    @Inject(at = @At("HEAD"), method = "isKeyDown", cancellable = true)
    private static void isCustomKeyDown(long window, int key, CallbackInfoReturnable<Boolean> cir) {
        if (key <= 0 && KBClientConfig.IGNORE_INVALID_KEY_CHECKS.get()) {
            var caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass)
                            .filter(c -> c != InputConstants.class)
                            .findFirst())
                            .orElse(null);
            if (caller != null && IGNORED_INVALID_CALLSITES.add(caller)) {
                LogUtils.getLogger().error("Class {} attempted to call InputConstants#isKeyDown with an invalid key code {}. This error will be suppressed for this class going forward", caller, key, new Throwable("Invalid key code " + key));
            }
            cir.setReturnValue(false);
            return;
        }

        // Our special key is always pressed as if a key mapping has it assigned it means it's currently selected
        if (key == ModKeyBindBundlesClient.SPECIAL_KEY_CODE) {
            cir.setReturnValue(true);
        }
    }
}

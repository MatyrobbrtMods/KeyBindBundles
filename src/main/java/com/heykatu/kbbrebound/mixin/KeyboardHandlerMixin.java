package com.heykatu.kbbrebound.mixin;

import com.heykatu.kbbrebound.KeyMappingUtil;
import com.heykatu.kbbrebound.render.KeybindSelectionOverlay;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(at = @At("HEAD"), method = "keyPress", cancellable = true)
    private void activateWithKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (Minecraft.getInstance().screen != null) return;
        if (key != GLFW.GLFW_KEY_SPACE && key != GLFW.GLFW_KEY_ENTER && key != GLFW.GLFW_KEY_KP_ENTER) return;
        // let releases through, they're harmless
        if (action == GLFW.GLFW_RELEASE) return;

        if (KeybindSelectionOverlay.INSTANCE.getDisplayedKeybind() != null) {
            if (action == GLFW.GLFW_PRESS) {
                KeybindSelectionOverlay.INSTANCE.activateHovered(InputConstants.Type.KEYSYM.getOrCreate(key));
            }
            // dont jump while the menu's up
            ci.cancel();
        } else if (KeyMappingUtil.isHeldActivationKey(key)) {
            // still holding it after picking, no jumping
            ci.cancel();
        }
    }
}

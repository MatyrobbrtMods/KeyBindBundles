package com.heykatu.kbbrebound;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeyMappingUtil {
    @Nullable
    public static KeyMapping getByName(String name) {
        return KeyMapping.ALL.get(name);
    }

    private static final Minecraft MC = Minecraft.getInstance();
    public static final List<KeyMapping> KEYS_TAKEN_OVER = new ArrayList<>();

    /**
     * This method emulates a press of the keymapping, firing the {@link InputEvent.Key key input event},
     * and handling special snowflakes like the fullscreen and screenshot buttons (see {@link KeyboardHandler#keyPress(long, int, int, int, int)})
     */
    public static void press(KeyMapping mapping) {
        if (mapping == MC.options.keyFullscreen) {
            MC.getWindow().toggleFullScreen();
            MC.options.fullscreen().set(MC.getWindow().isFullscreen());
            return;
        } else if (mapping == MC.options.keyScreenshot) {
            Screenshot.grab(
                    MC.gameDirectory,
                    MC.getMainRenderTarget(),
                    message -> MC.execute(() -> {
                        MC.gui.getChat().addClientSystemMessage(message);
                        MC.getNarrator().saySystemQueued(message);
                    })
            );
            return;
        }

        mapping.takeOverForBundle();
        KEYS_TAKEN_OVER.add(mapping);
        mapping.setDown(true);

        ClientHooks.onKeyInput(
                new KeyEvent(
                        ModKeyBindBundles.BUNDLE_TRIGGER_KEY.getValue(), 0, 0
                ), GLFW.GLFW_PRESS
        );
    }

    public static void click(KeyMapping map) {
        map.incrementClickCount();
    }

    public static void release(KeyMapping map) {
        map.setDown(false);
        ClientHooks.onKeyInput(
                new KeyEvent(
                        ModKeyBindBundles.BUNDLE_TRIGGER_KEY.getValue(), 0, 0
                ), GLFW.GLFW_RELEASE
        );

        map.restoreToOriginalKey();
        KEYS_TAKEN_OVER.remove(map);
        HELD_UNTIL_RELEASE.remove(map);
    }

    public static void restoreAll() {
        // held ones stay held, tickHeld lets them go
        KEYS_TAKEN_OVER.removeIf(keyMapping -> {
            if (HELD_UNTIL_RELEASE.containsKey(keyMapping)) return false;
            keyMapping.restoreToOriginalKey();
            if (keyMapping.isDown()) keyMapping.setDown(false);
            return true;
        });
    }

    // the real key that has to stay down for each mapping to stay pressed
    private static final Map<KeyMapping, InputConstants.Key> HELD_UNTIL_RELEASE = new LinkedHashMap<>();

    // press it and keep it pressed till the physical key comes up, even if a screen opens
    public static void pressHeld(KeyMapping mapping, InputConstants.Key physical) {
        // already held from a quick double press, pressing again would lose the real key
        if (HELD_UNTIL_RELEASE.containsKey(mapping)) {
            click(mapping);
            HELD_UNTIL_RELEASE.put(mapping, physical);
            return;
        }
        press(mapping);
        click(mapping);
        // fullscreen/screenshot dont get taken over so nothing to hold
        if (KEYS_TAKEN_OVER.contains(mapping)) {
            HELD_UNTIL_RELEASE.put(mapping, physical);
        }
    }

    public static void tickHeld() {
        if (HELD_UNTIL_RELEASE.isEmpty()) return;
        for (var entry : List.copyOf(HELD_UNTIL_RELEASE.entrySet())) {
            if (!isPhysicallyDown(entry.getValue())) {
                release(entry.getKey());
            }
        }
    }

    public static boolean isHeldActivationKey(int keyCode) {
        for (var physical : HELD_UNTIL_RELEASE.values()) {
            if (physical.getType() == InputConstants.Type.KEYSYM && physical.getValue() == keyCode) return true;
        }
        return false;
    }

    private static boolean isPhysicallyDown(InputConstants.Key key) {
        var window = MC.getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(window, key.getValue());
    }

    public static boolean isShiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}

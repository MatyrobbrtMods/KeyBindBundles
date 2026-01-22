package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.KBClientConfig;
import com.matyrobbrt.keybindbundles.KeyBindBundle;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindSelectionOverlay extends RadialMenuRenderer<KeyBindBundle.KeyEntry> implements LayeredDraw.Layer {
    public static final KeybindSelectionOverlay INSTANCE = new KeybindSelectionOverlay();

    @Nullable
    private KeyBindBundle displayedKeybind;
    private KeyBindBundleManager.RadialKeyMapping displayedMapping;

    @Nullable
    private KeyMapping currentlyPressing;

    @Override
    public Component getTitle(KeyBindBundle.KeyEntry entry) {
        return Component.literal(entry.title());
    }

    @Override
    public ItemStack getIcon(KeyBindBundle.KeyEntry entry) {
        return entry.icon();
    }

    @Override
    public List<KeyBindBundle.KeyEntry> getEntries() {
        return displayedKeybind == null ? List.of() : displayedKeybind.getEntries();
    }

    @Override
    public int getCurrentlySelected() {
        return displayedKeybind.getBookmark();
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (displayedKeybind == null) return;

        super.render(guiGraphics, true);

        if (KBClientConfig.CLIP_MOUSE_TO_MENU.getAsBoolean()) {
            var mainWindow = Minecraft.getInstance().getWindow();

            int windowWidth = mainWindow.getScreenWidth();
            int windowHeight = mainWindow.getScreenHeight();

            double[] xPos = new double[1];
            double[] yPos = new double[1];
            GLFW.glfwGetCursorPos(mainWindow.getWindow(), xPos, yPos);

            double scaledX = xPos[0] - (windowWidth / 2.0f);
            double scaledY = yPos[0] - (windowHeight / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = RadialMenuRenderer.OUTER * ((double) windowWidth / mainWindow.getGuiScaledWidth()) * 1.1;

            if (distance > radius) {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                GLFW.glfwSetCursorPos(mainWindow.getWindow(), (int) (windowWidth / 2 + fixedX), (int) (windowHeight / 2 + fixedY));
            }
        }
    }

    public void mouseClick(double mouseX, double mouseY, int button, int action) {
        var displayedKeybind = this.displayedKeybind;
        if (displayedKeybind == null) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && currentlyPressing != null && action == GLFW.GLFW_RELEASE) {
            KeyMappingUtil.release(currentlyPressing);
            currentlyPressing = null;
        }

        var window = Minecraft.getInstance().getWindow();
        float centerX = window.getGuiScaledWidth() / 2f;
        float centerY = window.getGuiScaledHeight() / 2f;

        double xDiff = mouseX - centerX;
        double yDiff = mouseY - centerY;
        double distanceFromCenter = Mth.length(xDiff, yDiff);
        if (distanceFromCenter < 10 || distanceFromCenter > RadialMenuRenderer.OUTER) {
            return;
        }

        int selectionIndex = getElementUnderMouse(false);

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_RELEASE) {
            if (selectionIndex == displayedKeybind.getBookmark()) {
                displayedKeybind.setBookmark(-1);
            } else {
                displayedKeybind.setBookmark(selectionIndex);
            }
            KeyBindBundleManager.write();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var key = KeyMappingUtil.getByName(displayedKeybind.getEntries().get(selectionIndex).key());
            if (key != null) {
                if (action == GLFW.GLFW_PRESS) {
                    KeyMappingUtil.press(key);
                    KeyMappingUtil.click(key);
                    currentlyPressing = key;
                }
            }
        }
    }

    @Nullable
    public KeyBindBundle getDisplayedKeybind() {
        return displayedKeybind;
    }

    @Nullable
    public KeyBindBundleManager.RadialKeyMapping getDisplayedMapping() {
        return displayedMapping;
    }

    public void open(KeyBindBundleManager.RadialKeyMapping mapping) {
        this.displayedKeybind = mapping.bind;
        this.displayedMapping = mapping;
    }

    public void close() {
        var kb = this.displayedKeybind;
        int underMouse = getElementUnderMouse(false);

        // Release keys before triggering them, to avoid scenarios in which triggering them would result
        // in a screen being set, which would infinitely release and trigger them again
        this.displayedKeybind = null;
        this.displayedMapping = null;
        clearState();

        if (kb != null && KBClientConfig.TRIGGER_KEYMAPPING_ON_RELEASE.getAsBoolean() && underMouse >= 0) {
            var key = KeyMappingUtil.getByName(kb.getEntries().get(underMouse).key());
            if (key != null) {
                KeyMappingUtil.press(key);
                KeyMappingUtil.click(key);
                // Delay release until next tick
                Minecraft.getInstance().execute(() -> KeyMappingUtil.release(key));
            }
        }

        KeyMappingUtil.restoreAll();
    }
}

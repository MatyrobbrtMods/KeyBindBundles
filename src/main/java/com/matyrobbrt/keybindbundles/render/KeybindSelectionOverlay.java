package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.KeyBindBundle;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindSelectionOverlay extends RadialMenuRenderer<KeyBindBundle.KeyEntry> implements net.minecraftforge.client.gui.overlay.IGuiOverlay {
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
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (displayedKeybind == null) return;

        super.render(guiGraphics, true);
    }

    public void mouseClick(double mouseX, double mouseY, int button, int action) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && currentlyPressing != null && action == GLFW.GLFW_RELEASE) {
            KeyMappingUtil.release(currentlyPressing);
            currentlyPressing = null;
        }
        if (displayedKeybind == null) return; // By releasing the mouse we might have opened a screen and therefore released all keybinds too

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
        this.displayedKeybind = null;
        this.displayedMapping = null;

        clearState();
        KeyMappingUtil.restoreAll();
    }
}

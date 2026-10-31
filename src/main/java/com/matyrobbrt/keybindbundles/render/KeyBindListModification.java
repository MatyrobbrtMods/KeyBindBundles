package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.KeyBindBundle;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.compat.ControllingCompat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class KeyBindListModification {
    public static void modify(KeyBindsList keyBindList, Minecraft minecraft) {
        if (KeyBundleModificationScreen.currentlySelecting != null) return;

        var addButton = new KeyBindsList.Entry() {
            private final Button addButton = new Button.Builder(Component.translatable("button.keybindbundles.add_bundle"), button -> {
                var oldScreen = minecraft.screen;
                minecraft.setScreen(new EditBoxScreen(name -> {
                    var newEntry = KeyBindBundleManager.add(new KeyBindBundle(UUID.randomUUID(), name, new ArrayList<>(), -1));

                    var newComponent = ControllingCompat.INSTANCE.createEntry(keyBindList, newEntry);

                    insertAfterLast(keyBindList, mapping -> mapping instanceof KeyBindBundleManager.RadialKeyMapping || mapping.getName().startsWith("key.keybindbundles."), newComponent, this);

                    minecraft.setScreen(oldScreen);
                }, Component.translatable("title.keybindbundles.adding_bundle"), Component.translatable("box.keybindbundles.bundle_name")));

            }).size(120, 20).build();
            private final List<? extends GuiEventListener> children = List.of(addButton);
            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                addButton.setPosition(left + width / 2 - addButton.getWidth() / 2, top - 2);
                addButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }

            @Override
            public void refreshEntry() {

            }
        };

        insertAfterLast(keyBindList, mapping -> !(mapping instanceof KeyBindBundleManager.RadialKeyMapping) && mapping.getName().startsWith("key.keybindbundles."), addButton, null);
    }

    private static void insertAfterLast(KeyBindsList list, Predicate<KeyMapping> previous, KeyBindsList.Entry after, @Nullable KeyBindsList.Entry alternativeAccepted) {
        int lastIndex = -1;
        for (int i = 0; i < list.children().size(); i++) {
            if (ControllingCompat.INSTANCE.testKey(list.children().get(i), previous) || list.children().get(i) == alternativeAccepted) {
                lastIndex = i;
            } else if (lastIndex != -1) {
                break;
            }
        }

        list.children().add(lastIndex + 1, after);
        ControllingCompat.INSTANCE.addChildren(list, lastIndex + 1, after);
    }
}

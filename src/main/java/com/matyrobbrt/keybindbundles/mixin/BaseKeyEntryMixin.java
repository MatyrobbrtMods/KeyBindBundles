package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KeyBindBundle;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.render.KeyBundleModificationScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(KeyBindsList.Entry.class)
public class BaseKeyEntryMixin {
    @Unique
    protected Button editButton;

    @Unique
    protected Button selectButton;

    protected void kbb$handleCustom(KeyMapping key, Component name) {
        if (key instanceof KeyBindBundleManager.RadialKeyMapping radial) {
            editButton = Button.builder(Component.translatable("button.keybindbundles.edit"), but -> {
                Minecraft.getInstance().setScreen(new KeyBundleModificationScreen(radial.bind));
            }).bounds(0, 0, 50, 20).build();
        }

        if (KeyBundleModificationScreen.currentlySelecting != null) {
            var isSelected = KeyBundleModificationScreen.currentlySelecting.getEntries()
                    .stream().anyMatch(e -> e.key().equals(key.getName()));

            selectButton = Button.builder(isSelected ? Component.translatable("button.keybindbundles.selected") : Component.translatable("button.keybindbundles.select"), but -> {
                KeyBundleModificationScreen.currentlySelecting.getEntries().add(new KeyBindBundle.KeyEntry(key.getName(), name.getString(), ItemStack.EMPTY));
                selectButton.setMessage(Component.translatable("button.keybindbundles.selected"));
                selectButton.active = false;
            }).bounds(0, 0, 50, 20).build();

            if (isSelected) selectButton.active = false;
        }
    }
}

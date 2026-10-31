package com.matyrobbrt.keybindbundles.compat;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

import java.util.function.Predicate;

public interface ControllingCompat {
    ControllingCompat INSTANCE = ModList.get().isLoaded("controlling") ? new ControllingCompatInstance() : new ControllingCompat() {
    };

    default void addChildren(KeyBindsList list, int index, KeyBindsList.Entry entry) {
    }

    default boolean testKey(KeyBindsList.Entry entry, Predicate<KeyMapping> test) {
        return entry instanceof KeyBindsList.KeyEntry ke && test.test(ke.key);
    }

    default KeyBindsList.Entry createEntry(KeyBindsList list, KeyMapping mapping) {
        return list.new KeyEntry(mapping, mapping.getDisplayName());
    }
}

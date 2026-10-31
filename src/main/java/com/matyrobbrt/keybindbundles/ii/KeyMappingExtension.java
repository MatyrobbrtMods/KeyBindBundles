package com.matyrobbrt.keybindbundles.ii;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface KeyMappingExtension {
    default void takeOverForBundle() {

    }

    default void restoreToOriginalKey() {

    }

    default void incrementClickCount() {

    }

    @Nullable
    default Component kbb$getNameOverride() {
        return null;
    }

    default Component kbb$getDisplayName() {
        return Component.empty();
    }

    default void kbb$unregister() {

    }
}

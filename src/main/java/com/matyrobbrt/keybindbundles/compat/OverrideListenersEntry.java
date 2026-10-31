package com.matyrobbrt.keybindbundles.compat;

import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public interface OverrideListenersEntry {
    List<GuiEventListener> getAdditionalListeners();

    boolean doOverrideListeners();
}

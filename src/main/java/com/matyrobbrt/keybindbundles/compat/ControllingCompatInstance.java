package com.matyrobbrt.keybindbundles.compat;

import com.blamejared.controlling.api.entries.IKeyEntry;
import com.blamejared.controlling.api.events.KeyEntryListenersEvent;
import com.blamejared.controlling.client.CustomList;
import com.blamejared.controlling.client.NewKeyBindsList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Predicate;

public class ControllingCompatInstance implements ControllingCompat {

    public ControllingCompatInstance() {
        NeoForge.EVENT_BUS.addListener((final KeyEntryListenersEvent event) -> {
            if (event.getEntry() instanceof OverrideListenersEntry et) {
                if (et.doOverrideListeners()) {
                    event.getListeners().clear();
                }
                event.getListeners().addAll(et.getAdditionalListeners());
            }
        });
    }

    @Override
    public void addChildren(KeyBindsList list, int index, KeyBindsList.Entry entry) {
        if (list instanceof CustomList cl) {
            cl.getAllEntries().add(index, entry);
        }
    }

    @Override
    public boolean testKey(KeyBindsList.Entry entry, Predicate<KeyMapping> test) {
        return entry instanceof IKeyEntry ke ? test.test(ke.getKey()) : (entry instanceof KeyBindsList.KeyEntry kk && test.test(kk.key));
    }

    @Override
    public KeyBindsList.Entry createEntry(KeyBindsList list, KeyMapping mapping) {
        if (list instanceof NewKeyBindsList nl) {
            return nl.new KeyEntry(mapping, Component.translatable(mapping.getName()));
        }
        return ControllingCompat.super.createEntry(list, mapping);
    }
}

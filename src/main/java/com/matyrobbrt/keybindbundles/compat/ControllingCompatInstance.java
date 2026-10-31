package com.matyrobbrt.keybindbundles.compat;

import com.blamejared.controlling.api.events.KeyEntryListenersEvent;
import com.blamejared.controlling.client.CustomList;
import com.blamejared.controlling.client.NewKeyBindsList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Constructor;
import java.util.function.Predicate;

public class ControllingCompatInstance implements ControllingCompat {
    private final Constructor<NewKeyBindsList.KeyEntry> ctor;

    public ControllingCompatInstance() {
        MinecraftForge.EVENT_BUS.addListener((final KeyEntryListenersEvent event) -> {
            if (event.getEntry() instanceof OverrideListenersEntry et) {
                if (et.doOverrideListeners()) {
                    event.getListeners().clear();
                }
                event.getListeners().addAll(et.getAdditionalListeners());
            }
        });

        try {
            ctor = NewKeyBindsList.KeyEntry.class.getDeclaredConstructor(NewKeyBindsList.class, KeyMapping.class, Component.class);
            ctor.setAccessible(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addChildren(KeyBindsList list, int index, KeyBindsList.Entry entry) {
        if (list instanceof CustomList cl) {
            cl.getAllEntries().add(index, entry);
        }
    }

    @Override
    public boolean testKey(KeyBindsList.Entry entry, Predicate<KeyMapping> test) {
        if (entry instanceof NewKeyBindsList.KeyEntry ke) {
            return test.test(ke.getKey());
        }
        return ControllingCompat.super.testKey(entry, test);
    }

    @Override
    public KeyBindsList.Entry createEntry(KeyBindsList list, KeyMapping mapping) {
        if (list instanceof NewKeyBindsList nl) {
            try {
                return ctor.newInstance(nl, mapping, Component.translatable(mapping.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ControllingCompat.super.createEntry(list, mapping);
    }
}

package com.matyrobbrt.keybindbundles;

import net.minecraft.client.KeyMapping;

public class PriorityKeyMapping extends KeyMapping {
    public PriorityKeyMapping(String name, int keyCode, String category) {
        super(name, keyCode, category);
    }

    @Override
    public int compareTo(KeyMapping map) {
        if (!(map instanceof PriorityKeyMapping)) return -1;
        return super.compareTo(map);
    }
}

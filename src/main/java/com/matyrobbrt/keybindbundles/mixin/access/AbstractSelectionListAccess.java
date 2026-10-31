package com.matyrobbrt.keybindbundles.mixin.access;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccess {
    @Invoker("getScrollbarPosition")
    int kbb$getScrollbarPosition();
}

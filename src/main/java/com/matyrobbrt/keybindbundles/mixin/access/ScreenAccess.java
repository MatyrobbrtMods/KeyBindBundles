package com.matyrobbrt.keybindbundles.mixin.access;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccess {
    @Invoker("repositionElements")
    void kbb$repositionElements();
}

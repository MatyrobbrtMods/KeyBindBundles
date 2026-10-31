package com.matyrobbrt.keybindbundles.mixin.access;

import net.minecraft.client.Options;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegisterKeyMappingsEvent.class)
public interface RegisterKeyMappingsEventAccess {
    @Accessor("options")
    Options kbb$getOptions();
}

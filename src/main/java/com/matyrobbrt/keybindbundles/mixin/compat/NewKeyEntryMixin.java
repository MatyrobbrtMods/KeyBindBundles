package com.matyrobbrt.keybindbundles.mixin.compat;

import com.blamejared.controlling.client.NewKeyBindsList;
import com.matyrobbrt.keybindbundles.compat.OverrideListenersEntry;
import com.matyrobbrt.keybindbundles.mixin.BaseKeyEntryMixin;
import com.matyrobbrt.keybindbundles.mixin.access.AbstractSelectionListAccess;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(value = NewKeyBindsList.KeyEntry.class)
public class NewKeyEntryMixin extends BaseKeyEntryMixin implements OverrideListenersEntry {
    @Shadow
    @Final
    private Button btnChangeKeyBinding;

    @Shadow
    @Final
    private NewKeyBindsList this$0;

    @Shadow
    @Mutable
    private Component keyDesc;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void handleCustom(NewKeyBindsList owner, KeyMapping key, Component name, CallbackInfo ci) {
        var customName = key.kbb$getNameOverride();
        if (customName != null) {
            name = customName;
            this.keyDesc = name;
        }

        kbb$handleCustom(key, name);
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(
            GuiGraphics guiGraphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovering,
            float partialTick,
            CallbackInfo ci
    ) {
        if (selectButton != null) {
            guiGraphics.drawString(Minecraft.getInstance().font, keyDesc, left, top + height / 2 - 9 / 2, -1);
            selectButton.setPosition(left + width - 5 - selectButton.getWidth(), top - 2);
            selectButton.render(guiGraphics, mouseX, mouseY, partialTick);
            ci.cancel();
        } else if (editButton != null) {
            editButton.setPosition(left + 105 - 5 - editButton.getWidth(), top);
            editButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public List<GuiEventListener> getAdditionalListeners() {
        if (selectButton != null) return List.of(selectButton);
        if (editButton != null) return List.of(editButton);
        return List.of();
    }

    @Override
    public boolean doOverrideListeners() {
        return selectButton != null;
    }
}

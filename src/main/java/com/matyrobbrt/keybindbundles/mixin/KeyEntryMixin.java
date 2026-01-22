package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.mixin.access.AbstractSelectionListAccess;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyEntryMixin extends BaseKeyEntryMixin {
    @Shadow
    @Final
    private Button changeButton;

    @Shadow
    @Final
    private KeyBindsList this$0;

    @Shadow
    @Final
    private Component name;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void handleCustom(KeyBindsList owner, KeyMapping key, Component name, CallbackInfo ci) {
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
            guiGraphics.drawString(Minecraft.getInstance().font, this.name, left, top + height / 2 - 9 / 2, -1);
            selectButton.setPosition(((AbstractSelectionListAccess) this$0).kbb$getScrollbarPosition() - selectButton.getWidth() - 10, top - 2);
            selectButton.render(guiGraphics, mouseX, mouseY, partialTick);
            ci.cancel();
        } else if (editButton != null) {
            int x = ((AbstractSelectionListAccess) this$0).kbb$getScrollbarPosition() - 50 - 10 - 5 - 75 - 5 - editButton.getWidth();
            editButton.setPosition(x, top - 2);
            editButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Inject(at = @At("RETURN"), method = {"children", "narratables"}, cancellable = true)
    private void addCustomChildren(CallbackInfoReturnable<List<GuiEventListener>> cir) {
        var newList = new ArrayList<>(cir.getReturnValue());
        if (selectButton != null) {
            newList.add(selectButton);
        } else if (editButton != null) {
            newList.add(editButton);
        }
        cir.setReturnValue(newList);
    }
}

package com.matyrobbrt.keybindbundles.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
public abstract class KeyEntryMixin extends BaseKeyEntryMixin {
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

    @Inject(at = @At("HEAD"), method = "extractContent", cancellable = true)
    private void extractContent(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a,
            CallbackInfo ci
    ) {
        if (selectButton != null) {
            graphics.text(Minecraft.getInstance().font, this.name, getContentX(), getContentY() + getHeight() / 2 - 9 / 2, -1);
            selectButton.setPosition(this$0.scrollBarX() - selectButton.getWidth() - 10, getContentY() - 2);
            selectButton.extractRenderState(graphics, mouseX, mouseY, a);
            ci.cancel();
        } else if (editButton != null) {
            int x = this$0.scrollBarX() - 50 - 10 - 5 - 75 - 5 - editButton.getWidth();
            editButton.setPosition(x, getContentY() - 2);
            editButton.extractRenderState(graphics, mouseX, mouseY, a);
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

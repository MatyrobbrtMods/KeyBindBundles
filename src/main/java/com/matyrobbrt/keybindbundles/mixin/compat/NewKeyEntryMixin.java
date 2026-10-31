package com.matyrobbrt.keybindbundles.mixin.compat;

import com.blamejared.controlling.client.NewKeyBindsList;
import com.matyrobbrt.keybindbundles.compat.OverrideListenersEntry;
import com.matyrobbrt.keybindbundles.mixin.BaseKeyEntryMixin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(NewKeyBindsList.KeyEntry.class)
public abstract class NewKeyEntryMixin extends BaseKeyEntryMixin implements OverrideListenersEntry {
    @Shadow
    @Final
    private NewKeyBindsList this$0;

    @Shadow
    @Final
    private Component name;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void handleCustom(NewKeyBindsList owner, KeyMapping key, Component name, CallbackInfo ci) {
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

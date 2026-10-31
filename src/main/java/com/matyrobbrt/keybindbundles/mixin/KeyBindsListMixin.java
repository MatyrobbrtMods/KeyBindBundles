package com.matyrobbrt.keybindbundles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.render.KeyBindListModification;
import com.matyrobbrt.keybindbundles.render.KeyBundleModificationScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {"net/minecraft/client/gui/screens/controls/KeyBindsList", "com/blamejared/controlling/client/NewKeyBindsList"})
public abstract class KeyBindsListMixin extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    private KeyBindsListMixin(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    @Inject(at = @At(value = "TAIL"), method = "<init>")
    private void addCustom(KeyBindsScreen keyBindsScreen, Minecraft minecraft, CallbackInfo ci) {
        KeyBindListModification.modify(((KeyBindsList) (Object) this), minecraft);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/ArrayUtils;clone([Ljava/lang/Object;)[Ljava/lang/Object;"))
    private Object[] removeBundleKeybinds(Object[] array, Operation<Object[]> operation) {
        if (KeyBundleModificationScreen.currentlySelecting != null) {
            var clone = new KeyMapping[array.length - KeyBindBundleManager.getKeys().size()];
            int i = 0;
            for (Object o : array) {
                if (!(o instanceof KeyBindBundleManager.RadialKeyMapping)) {
                    clone[i++] = (KeyMapping) o;
                }
            }
            return clone;
        }
        return operation.call((Object)array);
    }
}

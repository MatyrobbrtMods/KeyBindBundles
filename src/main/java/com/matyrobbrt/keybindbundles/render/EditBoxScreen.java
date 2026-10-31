package com.matyrobbrt.keybindbundles.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class EditBoxScreen extends Screen {
    private final Component message;
    private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;
    protected final Consumer<String> callback;

    public EditBoxScreen(Consumer<String> callback, Component title, Component message) {
        super(title);
        this.callback = callback;
        this.message = message;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
    }

    @Override
    protected void init() {
        super.init();
        this.multilineMessage = MultiLineLabel.create(this.font, this.message, this.width - 50);
        int y = Mth.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);

        var box = this.addRenderableWidget(new EditBox(
                minecraft.font, this.width / 2 - 150 / 2, y, 150, 20, message
        ) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    callback.accept(getValue());
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        });
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, p -> callback.accept(box.getValue()))
                .size(50, 20).pos(this.width / 2 - 50 / 2, y + 25).build());

        setFocused(box);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleTop(), 16777215);
        this.multilineMessage.renderCentered(guiGraphics, this.width / 2, this.messageTop());
    }

    private int titleTop() {
        int i = (this.height - this.messageHeight()) / 2;
        return Mth.clamp(i - 20 - 9, 10, 80);
    }

    private int messageTop() {
        return this.titleTop() + 20;
    }

    private int messageHeight() {
        return this.multilineMessage.getLineCount() * 9;
    }
}

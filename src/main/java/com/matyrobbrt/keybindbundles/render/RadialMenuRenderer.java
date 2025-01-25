package com.matyrobbrt.keybindbundles.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

// There's heavy inspiration from Mek, see https://github.com/mekanism/Mekanism/blob/1.21.x/src/main/java/mekanism/client/gui/GuiRadialSelector.java
public abstract class RadialMenuRenderer<T> {
    private static final float DRAWS = 300;

    public static final float INNER = 40, OUTER = 100;
    public static final float MIDDLE_DISTANCE = (INNER + OUTER) / 2F;

    public abstract List<T> getEntries();

    public abstract int getCurrentlySelected();

    public abstract Component getTitle(T entry);
    public abstract ItemStack getIcon(T entry);

    public boolean trackOffCircleMouse() {
        return true;
    }

    private int[] hoverGrows = new int[0];
    private long lastUpdate = System.currentTimeMillis();

    public void render(GuiGraphics guiGraphics, boolean trackMouse) {
        var entries = getEntries();
        if (entries.isEmpty()) return;

        if (hoverGrows.length < entries.size()) {
            hoverGrows = ArrayUtils.addAll(hoverGrows, IntStream.range(0, entries.size() - hoverGrows.length)
                    .map(i -> 0).toArray());
        }

        int count = entries.size();
        float angleSize = 360F / count;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float centerX = guiGraphics.guiWidth() / 2f;
        float centerY = guiGraphics.guiHeight() / 2f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0f);

        var hot = getCurrentlySelected();
        for (int i = 0; i < entries.size(); i++) {
            float startAngle = -90F + 360F * (-0.5F + i) / count;
            RadialMenuRenderer.drawTorus(guiGraphics, startAngle, angleSize, INNER, OUTER + 10f * (hoverGrows[i] / 10f), hot == i ? .7f : .3f, hot == i ? .4f : .3f, hot == i ? .45f : .3f, hot == i ? 0.7F : 0.6F);
        }

        if (trackMouse && !Minecraft.getInstance().mouseHandler.isMouseGrabbed()) {
            var underMouse = getElementUnderMouse(!trackOffCircleMouse());

            var current = System.currentTimeMillis();
            if (current >= (this.lastUpdate + 1000/40)) {
                lastUpdate = current;
                for (int i = 0; i < entries.size(); i++) {
                    if (i == underMouse) {
                        if (hoverGrows[i] < 10) hoverGrows[i]++;
                    } else if (hoverGrows[i] > 0) {
                        hoverGrows[i]--;
                    }
                }
            }
        }

        record PositionedText(float x, float y, Component text) {}
        List<PositionedText> textToDraw = new ArrayList<>(entries.size());

        float position = 0;
        for (var key : entries) {
            float degrees = 270 + 360 * (position++ / count);
            float angle = Mth.DEG_TO_RAD * degrees;
            float x = Mth.cos(angle) * RadialMenuRenderer.MIDDLE_DISTANCE;
            float y = Mth.sin(angle) * RadialMenuRenderer.MIDDLE_DISTANCE;

            var icon = getIcon(key);
            if (!icon.isEmpty()) {
                textToDraw.add(new PositionedText(x, y, getTitle(key)));

                guiGraphics.renderItem(icon, Math.round(x - 8), Math.round(y - 8 - 2 - 9));
            } else {
                textToDraw.add(new PositionedText(x, y - 4, getTitle(key)));
            }
        }

        if (!textToDraw.isEmpty()) {
            var font = Minecraft.getInstance().font;

            for (PositionedText toDraw : textToDraw) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(toDraw.x, toDraw.y, 0);
                guiGraphics.pose().scale(0.6F, 0.6F, 0.6F);
                Component text = toDraw.text;
                guiGraphics.drawString(font, text.getVisualOrderText(), -font.width(text) / 2F, 8, 0xCCFFFFFF, true);
                guiGraphics.pose().popPose();
            }
        }

        guiGraphics.pose().popPose();

        RenderSystem.disableBlend();
    }

    public record MousePos(double x, double y) {}

    public MousePos getMousePos() {
        var mouse = Minecraft.getInstance().mouseHandler;
        double mouseX = mouse.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth();
        double mouseY = mouse.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();
        return new MousePos(mouseX, mouseY);
    }

    protected int getElementUnderMouse(boolean upperboundRadius) {
        var mouse = getMousePos();
        var mouseX = mouse.x;
        var mouseY = mouse.y;
        var count = getEntries().size();

        var window = Minecraft.getInstance().getWindow();
        float centerX = window.getGuiScaledWidth() / 2f;
        float centerY = window.getGuiScaledHeight() / 2f;

        double xDiff = mouseX - centerX;
        double yDiff = mouseY - centerY;
        double distanceFromCenter = Mth.length(xDiff, yDiff);
        if (distanceFromCenter > 10) {
            if (upperboundRadius && distanceFromCenter > RadialMenuRenderer.OUTER) return -1;
            // draw mouse selection highlight
            float angle = (float) (Mth.RAD_TO_DEG * Mth.atan2(yDiff, xDiff));
            float modeSize = 180F / count;

            float selectionAngle = RadialMenuRenderer.wrapDegrees(angle + modeSize + 90F);
            return (int) (selectionAngle * (count / 360F));
        }
        return -1;
    }

    protected void clearState() {
        hoverGrows = new int[0];
    }

    private static void drawTorus(GuiGraphics guiGraphics, float startAngle, float sizeAngle, float inner, float outer, float red, float green, float blue, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var vertexBuffer = Tesselator.getInstance().getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        var matrix4f = guiGraphics.pose().last().pose();
        float draws = DRAWS * (sizeAngle / 360F);
        for (int i = 0; i <= draws; i++) {
            float degrees = startAngle + (i / DRAWS) * 360;
            float angle = Mth.DEG_TO_RAD * degrees;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            vertexBuffer.vertex(matrix4f, outer * cos, outer * sin, 0)
                    .color(red, green, blue, alpha).endVertex();
            vertexBuffer.vertex(matrix4f, inner * cos, inner * sin, 0)
                    .color(red, green, blue, alpha).endVertex();
        }
        BufferUploader.drawWithShader(vertexBuffer.end());
    }

    public static float wrapDegrees(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
}

package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.KBClientConfig;
import com.matyrobbrt.keybindbundles.ModKeyBindBundles;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

// There's heavy inspiration from Mek, see https://github.com/mekanism/Mekanism/blob/1.21.x/src/main/java/mekanism/client/gui/GuiRadialSelector.java
public abstract class RadialMenuRenderer<T> {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(ModKeyBindBundles.MOD_ID, "radial"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .build();

    private static final float DRAWS = 300;

    public static final float INNER = 40, OUTER = 100;
    public static final float MIDDLE_DISTANCE = (INNER + OUTER) / 2F;

    private static final float[] BLUE = {0.15F, 0.35F, 0.85F};
    private static final float[] TEAL = {0.1F, 0.65F, 0.65F};
    private static final float[] INDIGO = {0.25F, 0.2F, 0.7F};
    private static final float[] HOVERED = {1.0F, 0.55F, 0.1F};
    private static final float[] BOOKMARKED = {0.85F, 0.1F, 0.15F};

    private static float[] segmentColor(int i, int count, int hovered, int bookmarked) {
        if (i == hovered) return HOVERED;
        if (i == bookmarked) return BOOKMARKED;
        // odd count means last and first touch, don't let them both be blue
        if (count > 1 && count % 2 == 1 && i == count - 1) return INDIGO;
        return i % 2 == 0 ? BLUE : TEAL;
    }

    public abstract List<T> getEntries();

    public abstract int getCurrentlySelected();

    public abstract Component getTitle(T entry);
    public abstract ItemStack getIcon(T entry);

    public boolean trackOffCircleMouse() {
        return true;
    }

    private int[] hoverGrows = new int[0];
    private long lastUpdate = System.currentTimeMillis();

    public void render(GuiGraphicsExtractor guiGraphics, boolean trackMouse) {
        var entries = getEntries();
        if (entries.isEmpty()) return;

        if (hoverGrows.length < entries.size()) {
            hoverGrows = ArrayUtils.addAll(hoverGrows, IntStream.range(0, entries.size() - hoverGrows.length)
                    .map(i -> 0).toArray());
        }

        int count = entries.size();
        float angleSize = 360F / count;

        float centerX = guiGraphics.guiWidth() / 2f;
        float centerY = guiGraphics.guiHeight() / 2f;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(centerX, centerY);

        boolean tracking = trackMouse && !Minecraft.getInstance().mouseHandler.isMouseGrabbed();
        int underMouse = tracking ? getElementUnderMouse(!trackOffCircleMouse()) : -1;

        var hot = getCurrentlySelected();
        for (int i = 0; i < entries.size(); i++) {
            float startAngle = -90F + 360F * (-0.5F + i) / count;
            float[] color = segmentColor(i, count, underMouse, hot);
            boolean highlighted = i == underMouse || i == hot;
            RadialMenuRenderer.drawTorus(guiGraphics, startAngle, angleSize, INNER, OUTER + 10f * (hoverGrows[i] / 10f), color[0], color[1], color[2], highlighted ? 0.7F : 0.6F);
        }

        if (tracking) {
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

        float fontScale = (float)KBClientConfig.FONT_SIZE.getAsDouble();
        var font = Minecraft.getInstance().font;

        for (int i = 0; i < entries.size(); i++) {
            var key = entries.get(i);
            float degrees = 270 + 360 * ((float)i / count);
            float angle = Mth.DEG_TO_RAD * degrees;
            float x = Mth.cos(angle) * RadialMenuRenderer.MIDDLE_DISTANCE;
            float y = Mth.sin(angle) * RadialMenuRenderer.MIDDLE_DISTANCE;

            var icon = getIcon(key);
            var text = getTitle(key);
            var width = getTextWidth(x, y + (icon.isEmpty() ? -4 : 0) + 8 * fontScale, degrees, angleSize, fontScale);

            var lines = font.split(text, width);
            int firstLineY = (icon.isEmpty() ? 0 : 8) - (lines.size() - 1) * font.lineHeight / 2;

            if (!icon.isEmpty()) {
                guiGraphics.item(icon, Math.round(x - 8), Math.round(y + firstLineY - 4 - 16));
            }

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(x, y);
            guiGraphics.pose().scale(fontScale, fontScale);
            for (int line = 0; line < lines.size(); line++) {
                var lineText = lines.get(line);
                guiGraphics.text(font, lineText, -font.width(lineText) / 2, firstLineY + line * font.lineHeight, 0xCCFFFFFF, true);
            }
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().popMatrix();
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

    private static void drawTorus(GuiGraphicsExtractor guiGraphics, float startAngle, float sizeAngle, float inner, float outer, float red, float green, float blue, float alpha) {
        var matrix4f = new Matrix3x2f(guiGraphics.pose());
        var bounds = new ScreenRectangle((int) Math.ceil(-outer), (int) Math.ceil(-outer), (int) Math.ceil(2 * outer), (int) Math.ceil(2 * outer)).transformMaxBounds(matrix4f);
        guiGraphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                float draws = DRAWS * (sizeAngle / 360F);
                for (int i = 0; i <= draws; i++) {
                    float degrees = startAngle + (i / DRAWS) * 360;
                    float angle = Mth.DEG_TO_RAD * degrees;
                    float cos = Mth.cos(angle);
                    float sin = Mth.sin(angle);
                    vertexConsumer.addVertexWith2DPose(matrix4f, outer * cos, outer * sin)
                            .setColor(red, green, blue, alpha);
                    vertexConsumer.addVertexWith2DPose(matrix4f, inner * cos, inner * sin)
                            .setColor(red, green, blue, alpha);
                }
            }

            @Override
            public RenderPipeline pipeline() {
                return PIPELINE;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public @Nullable ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public @Nullable ScreenRectangle bounds() {
                return bounds;
            }
        });
    }

    public static float wrapDegrees(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private static int getTextWidth(float x, float y, float centerDegrees, float angleSize, float fontScale) {
        float halfAngle = angleSize / 2;
        float left = horizontalDistanceToSliceEdge(x, y, centerDegrees, halfAngle, -1);
        float right = horizontalDistanceToSliceEdge(x, y, centerDegrees, halfAngle, 1);
        return Math.max(1, Mth.floor((left + right - 8) / fontScale));
    }
    private static float horizontalDistanceToSliceEdge(float x, float y, float centerDegrees, float halfAngle, int direction) {
        float low = 0;
        float high = OUTER + MIDDLE_DISTANCE;
        for (int i = 0; i < 16; i++) {
            float distance = (low + high) / 2;
            float candidateX = x + direction * distance;
            float candidateAngle = (float) (Mth.RAD_TO_DEG * Mth.atan2(y, candidateX));
            float angleDifference = wrapDegrees(candidateAngle - centerDegrees + 180) - 180;
            boolean inside = Mth.sqrt(candidateX * candidateX + y * y) <= OUTER
                    && Math.abs(angleDifference) <= halfAngle;
            if (inside) {
                low = distance;
            } else {
                high = distance;
            }
        }
        return low;
    }
}

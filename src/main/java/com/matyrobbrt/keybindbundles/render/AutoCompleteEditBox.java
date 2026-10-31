package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.util.DelegatingConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AutoCompleteEditBox<T> extends EditBox {
    private final SearchTree<T> tree;
    private final Function<T, ResourceLocation> idGetter;
    private final int maxSuggestions;

    private final DelegatingConsumer<String> responders;
    private final AutoComplete autoComplete;
    public AutoCompleteEditBox(Font font, int x, int y, int width, int height, int itemHeight, int itemWidth, int maxSuggestions, Component message, SearchTree<T> tree, Function<T, ResourceLocation> idGetter) {
        super(font, x, y, width, height, message);
        this.tree = tree;
        this.idGetter = idGetter;
        this.maxSuggestions = maxSuggestions;
        setResponder(responders = new DelegatingConsumer<>());

        addResponder(autoComplete = new AutoComplete(x, y + 2 + height, width, itemHeight, itemWidth));

        setFormatter((search, cursor) -> {
            if (search.indexOf('@') >= 0) {
                var comp = Component.empty();
                var spl = search.split(" ");
                for (int i = 0; i < spl.length; i++) {
                    if (i != 0) comp = comp.append(Component.literal(" "));

                    if (spl[i].startsWith("@")) {
                        comp = comp.append(Component.literal(spl[i]).withStyle(ChatFormatting.GOLD));
                    } else {
                        comp = comp.append(Component.literal(spl[i]));
                    }
                }

                int trailingAmount = 0;
                int lastIndex = search.length() - 1;
                while (search.charAt(lastIndex--) == ' ') trailingAmount++;
                if (trailingAmount > 0) comp = comp.append(Component.literal(" ".repeat(trailingAmount)));

                return comp.getVisualOrderText();
            }
            return Component.literal(search).getVisualOrderText();
        });
    }

    public void addResponder(Consumer<String> res) {
        responders.add(res);
    }

    public abstract void renderItem(GuiGraphics graphics, int x, int y, T item);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setValue("");
            autoComplete.accept("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ENTER && autoComplete().selectedIndex >= 0) {
            var item = autoComplete.getSuggestion(autoComplete.offset + autoComplete.selectedIndex);
            if (item != null) {
                setValue(idGetter.apply(item).toString());
                return true;
            }
        } else if (key == GLFW.GLFW_KEY_DOWN) {
            autoComplete.scrollDown();
            return true;
        } else if (key == GLFW.GLFW_KEY_UP) {
            autoComplete.scrollUp();
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    public AutoComplete autoComplete() {
        return autoComplete;
    }

    public class AutoComplete extends AbstractWidget implements Consumer<String> {
        private List<T> currentSuggestions = List.of();
        private final Vector2d lastMousePosition = new Vector2d(0, 0);

        private final int itemHeight, itemWidth;

        private int selectedIndex, offset;
        
        private String prevSearch;

        public AutoComplete(int x, int y, int width, int itemHeight, int itemWidth) {
            super(x, y, width, itemHeight * maxSuggestions, Component.empty());
            this.itemHeight = itemHeight;
            this.itemWidth = itemWidth;
        }

        @Override
        public void accept(String search) {
            if (Objects.equals(search, prevSearch) || search.isBlank()) return;
            
            prevSearch = search;
            offset = 0;
            selectedIndex = 0;
            
            var asRl = ResourceLocation.tryParse(search);

            String namespaceFilter;
            if (search.indexOf('@') >= 0) {
                var spl = new ArrayList<>(Arrays.asList(search.split(" ")));
                var element = spl.stream()
                        .filter(s -> s.startsWith("@"))
                        .findFirst()
                        .orElse(null);
                if (element == null) {
                    namespaceFilter = "";
                } else {
                    spl.remove(element);
                    namespaceFilter = element.substring(1).toLowerCase(Locale.ROOT);
                    search = String.join(" ", spl);
                }
            } else {
                namespaceFilter = "";
            }

            var items = tree.search(search.trim().toLowerCase(Locale.ROOT));

            var distinctSet = new HashSet<ResourceLocation>();
            var newItems = new ArrayList<T>(items.size());
            for (T item : items) {
                if (distinctSet.add(idGetter.apply(item))) {
                    newItems.add(item);
                }
            }
            items = newItems;

            if (asRl != null && items.stream().anyMatch(i -> idGetter.apply(i).equals(asRl))) {
                currentSuggestions = List.of();
            } else {
                if (namespaceFilter.isBlank()) {
                    currentSuggestions = items;
                } else {
                    currentSuggestions = items.stream().filter(i -> idGetter.apply(i).getNamespace().startsWith(namespaceFilter))
                            .toList();
                }
            }
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (AutoCompleteEditBox.this.isFocused()) {
                updateHoveringState(mouseX, mouseY);

                for (int i = offset; i < Math.min(offset + shownSuggestions(), currentSuggestions.size()); i++) {
                    int minX = this.getX() + 2;
                    int minY = this.getY() + itemHeight * (i - offset);
                    int maxY = minY + itemHeight;
                    var item = currentSuggestions.get(i);
                    var hovered = i - offset == selectedIndex;
                    guiGraphics.fill(RenderType.guiOverlay(), this.getX(), minY, this.getX() + this.getWidth(), maxY, hovered ? -535752431 : -536870912);
                    renderItem(guiGraphics, minX, minY, item);
                    guiGraphics.drawString(Minecraft.getInstance().font, Component.literal(idGetter.apply(item).toString()), minX + itemWidth + 2, minY + (itemHeight - 9) / 2, hovered ? ChatFormatting.YELLOW.getColor() : -1);
                }
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        private void updateHoveringState(double x, double y) {
            if (!this.lastMousePosition.equals(x, y)) {
                this.selectedIndex = -1;
                if (this.isMouseOver(x, y)) {
                    int minY = this.getY();

                    for (int i = 0; i < this.shownSuggestions(); ++i) {
                        int maxY = minY + itemHeight;
                        if (x >= this.getX() && x <= (this.getX() + this.getWidth()) && y >= minY && y < maxY) {
                            this.selectedIndex = i;
                        }

                        minY = maxY;
                    }
                }
                lastMousePosition.set(x, y);
            }
        }

        @Nullable
        private T getSuggestion(int index) {
            return index >= currentSuggestions.size() ? null : currentSuggestions.get(index);
        }

        private int shownSuggestions() {
            return Math.min(maxSuggestions, currentSuggestions.size());
        }

        private void scrollUp() {
            offsetDisplay(selectedIndex - 1);
        }

        private void scrollDown() {
            offsetDisplay(selectedIndex + 1);
        }

        private void offsetDisplay(int offset) {
            offset = Mth.clamp(offset, 0, shownSuggestions() - 1);
            final int halfSuggestions = Math.floorDiv(maxSuggestions, 2);
            int currentItem = this.offset + offset;
            if (currentItem < this.offset + halfSuggestions) {
                this.offset = Math.max(currentItem - halfSuggestions, 0);
            } else if (currentItem > this.offset + halfSuggestions) {
                this.offset = Math.min(currentItem - halfSuggestions, Math.max(this.currentSuggestions.size() - maxSuggestions, 0));
            }
            this.selectedIndex = currentItem - this.offset;
        }

        @Override
        public boolean mouseScrolled(double xpos, double ypos, double delta) {
            if (!this.isMouseOver(xpos, ypos)) {
                return false;
            } else {
                this.offset = (int) Mth.clamp((double)this.offset - delta, 0.0, Math.max(this.currentSuggestions.size() - maxSuggestions, 0));
                this.lastMousePosition.set(0.0);
                return true;
            }
        }

        @Override
        protected boolean clicked(double xpos, double ypos) {
            return super.clicked(xpos, ypos) && ypos < getY() + shownSuggestions() * itemHeight;
        }

        @Override
        public boolean isMouseOver(double xpos, double ypos) {
            return super.isMouseOver(xpos, ypos) && ypos < getY() + shownSuggestions() * itemHeight;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int mb) {
            if (super.mouseClicked(mx, my, mb)) {
                updateHoveringState(mx, my);
                if (selectedIndex != -1) {
                    var item = getSuggestion(offset + selectedIndex);
                    if (item != null) {
                        setValue(idGetter.apply(item).toString());
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }
}

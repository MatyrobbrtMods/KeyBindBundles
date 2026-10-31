package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.KeyBindBundle;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import com.matyrobbrt.keybindbundles.util.SearchTreeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class KeyBundleModificationScreen extends Screen {
    public static KeyBindBundle currentlySelecting;

    private static final KeyBindBundle.KeyEntry ADD_ENTRY = new KeyBindBundle.KeyEntry("add", "§6Add Entry§r");
    private static final List<ClientTooltipComponent> TOOLTIPS = Stream.of(
            Component.translatable("tooltip.keybindbundles.bundle.edit_entry"),
            Component.translatable("tooltip.keybindbundles.bundle.delete_entry"),
            Component.translatable("tooltip.keybindbundles.bundle.move_clockwise"),
            Component.translatable("tooltip.keybindbundles.bundle.move_counterclockwise")
    ).map(c -> ClientTooltipComponent.create(c.getVisualOrderText())).toList();
    private static final WidgetSprites CROSS_BUTTON_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/cross_button"), Identifier.withDefaultNamespace("widget/cross_button_highlighted")
    );

    private final RadialMenuRenderer<KeyBindBundle.KeyEntry> renderer = new RadialMenuRenderer<>() {
        @Override
        public List<KeyBindBundle.KeyEntry> getEntries() {
            return entries;
        }

        @Override
        public int getCurrentlySelected() {
            return bundle.getBookmark();
        }

        @Override
        public Component getTitle(KeyBindBundle.KeyEntry entry) {
            return Component.literal(entry.title());
        }

        @Override
        public ItemStack getIcon(KeyBindBundle.KeyEntry entry) {
            return entry.icon().areComponentsBound() ? entry.icon().value().getDefaultInstance() : ItemStack.EMPTY;
        }

        @Override
        public boolean trackOffCircleMouse() {
            return false;
        }
    };

    private final KeyBindBundle bundle;
    private List<KeyBindBundle.KeyEntry> entries = List.of();

    public KeyBundleModificationScreen(KeyBindBundle bundle) {
        super(Component.translatable("title.keybindbundles.editing_keybundle", Component.literal(bundle.name).withStyle(ChatFormatting.GOLD)));
        this.bundle = bundle;
    }

    @Override
    protected void init() {
        super.init();
        entries = new ArrayList<>(bundle.getEntries());
        entries.add(ADD_ENTRY);

        var deleteButton = this.addRenderableWidget(
                new ImageButton(width - 18, height - 18, 14, 14, CROSS_BUTTON_SPRITES, but -> {
                    Minecraft.getInstance().setScreen(new ConfirmScreen(click -> {
                        if (click) {
                            currentlySelecting = null;
                            Minecraft.getInstance().setScreen(null);
                            KeyBindBundleManager.delete(bundle);
                        } else {
                            Minecraft.getInstance().setScreen(this);
                        }
                    }, Component.translatable("title.keybindbundles.confirm_deletion"), Component.translatable("message.keybindbundles.confirm_deletion")));
                }, Component.translatable("button.keybindbundles.delete"))
        );
        deleteButton.setTooltip(Tooltip.create(Component.translatable("tooltip.keybindbundles.delete_bundle")));
    }

    @Override
    public void onClose() {
        KeyBindBundleManager.write();
        currentlySelecting = null;
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(font, getTitle(), width / 2 - font.width(getTitle()) / 2, 10, 0xffffffff);

        var render = Minecraft.getInstance().screen == this;
        renderer.render(graphics, render);
        if (render) {
            var element = renderer.getElementUnderMouse(true);
            if (element >= 0 && element < entries.size() - 1) {
                graphics.tooltip(font, TOOLTIPS, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        var element = renderer.getElementUnderMouse(true);
        if (element < 0) return super.mouseClicked(event, doubleClick);

        if (element == entries.size() - 1) {
            currentlySelecting = bundle;
            Minecraft.getInstance().setScreen(new KeyBindsScreen(
                    this, Minecraft.getInstance().options
            ));
        } else {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (KeyMappingUtil.isShiftDown()) {
                    if (bundle.getBookmark() == element) {
                        bundle.setBookmark(-1);
                    }

                    entries.remove(element);
                    bundle.getEntries().remove(element);
                } else {
                    var key = KeyMappingUtil.getByName(bundle.getEntries().get(element).key());
                    Minecraft.getInstance().pushGuiLayer(new EditKeyScreen(element, key == null ? Component.empty() : Component.translatable("title.keybindbundles.editing_key", key.getDisplayName().copy().withStyle(ChatFormatting.GOLD))));
                }
            } else {
                int newPos;
                if (KeyMappingUtil.isShiftDown()) {
                    newPos = element - 1;
                    if (newPos < 0) {
                        newPos = bundle.getEntries().size() - 1;
                    }
                } else {
                    newPos = element + 1;
                    if (newPos >= bundle.getEntries().size()) {
                        newPos = 0;
                    }
                }

                Collections.swap(entries, element, newPos);
                Collections.swap(bundle.getEntries(), element, newPos);
            }
        }

        return true;
    }

    public class EditKeyScreen extends Screen {
        private final int key;
        private int middlePos;
        protected EditKeyScreen(int key, Component title) {
            super(title);
            this.key = key;
        }

        private EditBox title;
        private AutoCompleteEditBox<ItemStack> icon;

        @Override
        protected void init() {
            var font = Minecraft.getInstance().font;
            middlePos = height / 2 - 20;
            title = new EnterEditBox(font, width / 2 - 120, middlePos - 2 - 20, 240, 20, Component.translatable("box.keybindbundles.key_title")) {
                @Override
                protected void onEnter() {
                    EditKeyScreen.this.setFocused(icon);
                }
            };
            title.setValue(getEntry().title());

            SearchTree<ItemStack> tree = SearchTreeManager.getSearchTree();
            icon = new AutoCompleteEditBox<>(font, width / 2 - 120, middlePos + 2 + 9 + 2, 240, 20, 16, 18, 5, Component.translatable("box.keybindbundles.key_icon_id"), tree, i -> BuiltInRegistries.ITEM.wrapAsHolder(i.getItem()).getKey().identifier()) {
                @Override
                public void renderItem(GuiGraphicsExtractor graphics, int x, int y, ItemStack item) {
                    graphics.item(item, x, y);
                }
            };
            icon.setMaxLength(512);
            if (getEntry().icon().value() != Items.AIR) {
                icon.setValue(getEntry().icon().getRegisteredName());
            }

            addRenderableWidget(title);
            addRenderableWidget(icon);

            addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, p -> onClose())
                    .pos(width / 2 - 240 / 2, icon.autoComplete().getY() + icon.autoComplete().getHeight() + 10)
                    .size(240, 20)
                    .build());

            addRenderableWidget(icon.autoComplete());
        }

        @Override
        protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        public boolean isInGameUi() {
            return true;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            super.extractRenderState(graphics, mouseX, mouseY, a);
            var font = Minecraft.getInstance().font;

            graphics.text(font, getTitle(), width / 2 - font.width(getTitle()) / 2, 30, 0xffffffff);

            var title = Component.translatable("box.keybindbundles.key_title");
            graphics.text(Minecraft.getInstance().font, title, width / 2 - font.width(title) / 2, middlePos - 2 - 20 - 2 - 9, 0xffffffff);

            var icon = Component.translatable("box.keybindbundles.key_icon");
            graphics.text(Minecraft.getInstance().font, icon, width / 2 - font.width(icon) / 2, middlePos + 2, 0xffffffff);

            if (!this.icon.getValue().isEmpty()) {
                var parsed = Identifier.tryParse(this.icon.getValue());
                if (parsed == null) return;
                var item = BuiltInRegistries.ITEM.get(parsed).orElse(null);
                if (item != null && item.value() != Items.AIR && item.areComponentsBound()) {
                    graphics.item(item.value().getDefaultInstance(), this.icon.getX() + this.icon.getWidth() + 2, middlePos + 1 + 9 + 2 + (20 - 16) / 2);
                }
            }
        }

        @Override
        public void onClose() {
            var old = getEntry();
            var parsed = Identifier.tryParse(this.icon.getValue());
            var newEntry = new KeyBindBundle.KeyEntry(old.key(), title.getValue(),
                    parsed == null ? Items.AIR.builtInRegistryHolder() : BuiltInRegistries.ITEM.getValue(parsed).builtInRegistryHolder());
            entries.set(key, newEntry);
            bundle.getEntries().set(key, newEntry);

            Minecraft.getInstance().setScreen(KeyBundleModificationScreen.this);
        }

        public KeyBindBundle.KeyEntry getEntry() {
            return entries.get(key);
        }

        protected class EnterEditBox extends EditBox {
            public EnterEditBox(Font font, int x, int y, int width, int height, Component message) {
                super(font, x, y, width, height, message);
            }

            @Override
            public boolean keyPressed(KeyEvent event) {
                if (event.key() == GLFW.GLFW_KEY_ENTER) {
                    onEnter();
                    return true;
                }
                return super.keyPressed(event);
            }

            protected void onEnter() {
                onClose();
            }
        }
    }

}

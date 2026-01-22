package com.matyrobbrt.keybindbundles;

import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import com.matyrobbrt.keybindbundles.util.SearchTreeManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

@Mod(value = ModKeyBindBundles.MOD_ID, dist = Dist.CLIENT)
public class ModKeyBindBundles {
    public static final String MOD_ID = "keybindbundles";
    public static final KeyMapping OPEN_RADIAL_MENU_MAPPING = new PriorityKeyMapping(
            "key.keybindbundles.open_radial_menu",
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.keybindbundles"
    ) {
        @Override
        public int compareTo(KeyMapping map) {
            return map instanceof KeyBindBundleManager.RadialKeyMapping ? -1 : super.compareTo(map);
        }
    };

    public static final KeyMapping OPEN_SCREEN_MAPPING = new PriorityKeyMapping(
            "key.keybindbundles.open_screen",
            GLFW.GLFW_KEY_UNKNOWN,
            "category.keybindbundles"
    ) {
        @Override
        public void setDown(boolean value) {
            if (isDown() && !value && Minecraft.getInstance().screen == null) {
                super.setDown(false);
                Minecraft.getInstance().setScreen(new KeyBindsScreen(new Screen(Component.empty()) {
                    @Override
                    protected void init() {
                        Minecraft.getInstance().setScreen(null);
                    }
                }, Minecraft.getInstance().options));
            } else {
                super.setDown(value);
            }
        }

        @Override
        public int compareTo(KeyMapping map) {
            return map instanceof KeyBindBundleManager.RadialKeyMapping ? -1 : super.compareTo(map);
        }
    };

    // Random number chosen by fair dice roll. Pray mods get along with keys that don't exist
    public static final int SPECIAL_KEY_CODE = 22745;

    // A random key constant we use to simulate our presses when mimicking InputEvent.Key
    public static final InputConstants.Key BUNDLE_TRIGGER_KEY = InputConstants.getKey(SPECIAL_KEY_CODE, -1);

    public ModKeyBindBundles(ModContainer container, IEventBus bus) {
        bus.addListener((final FMLClientSetupEvent event) -> event.enqueueWork(KeyBindBundleManager::load));

        bus.addListener((final RegisterGuiLayersEvent event) -> {
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "keybind_selection"), KeybindSelectionOverlay.INSTANCE);
        });

        bus.addListener(EventPriority.LOWEST, (final RegisterKeyMappingsEvent event) -> {
            Minecraft.getInstance().options.keyMappings = ArrayUtils.insert(0, Minecraft.getInstance().options.keyMappings,
                    OPEN_RADIAL_MENU_MAPPING, OPEN_SCREEN_MAPPING);
        });

        NeoForge.EVENT_BUS.addListener((final InputEvent.MouseButton.Pre event) -> {
            // Only handle right and left clicks, and allow minecraft to process buttons like Button 4 normally (see https://github.com/MatyrobbrtMods/KeyBindBundles/issues/28)
            if (KeybindSelectionOverlay.INSTANCE.getDisplayedKeybind() != null && Minecraft.getInstance().screen == null && event.getButton() <= GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                var mouse = Minecraft.getInstance().mouseHandler;
                double mouseX = mouse.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth();
                double mouseY = mouse.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();

                KeybindSelectionOverlay.INSTANCE.mouseClick(mouseX, mouseY, event.getButton(), event.getAction());
                event.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener((final ScreenEvent.Opening event) -> KeyMappingUtil.restoreAll());

        NeoForge.EVENT_BUS.addListener(SearchTreeManager::onPlayerJoin);

        container.registerConfig(ModConfig.Type.CLIENT, KBClientConfig.SPEC, MOD_ID + "-client.toml");
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

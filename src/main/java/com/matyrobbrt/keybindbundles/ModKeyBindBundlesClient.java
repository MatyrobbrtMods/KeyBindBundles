package com.matyrobbrt.keybindbundles;

import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import com.matyrobbrt.keybindbundles.util.SearchTreeManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindBundlesClient {
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
            if (isDown() && !value) {
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

    public static void init(IEventBus bus) {
        bus.addListener((final FMLClientSetupEvent event) -> event.enqueueWork(KeyBindBundleManager::load));

        bus.addListener((final RegisterGuiOverlaysEvent event) -> {
            event.registerAboveAll("keybind_selection", KeybindSelectionOverlay.INSTANCE);
        });

        bus.addListener(EventPriority.LOWEST, (final RegisterKeyMappingsEvent event) -> {
            Minecraft.getInstance().options.keyMappings = ArrayUtils.insert(0, Minecraft.getInstance().options.keyMappings,
                    OPEN_RADIAL_MENU_MAPPING, OPEN_SCREEN_MAPPING);
        });

        MinecraftForge.EVENT_BUS.addListener((final InputEvent.MouseButton.Pre event) -> {
            if (KeybindSelectionOverlay.INSTANCE.getDisplayedKeybind() != null && Minecraft.getInstance().screen == null) {
                var mouse = Minecraft.getInstance().mouseHandler;
                double mouseX = mouse.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth();
                double mouseY = mouse.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();

                KeybindSelectionOverlay.INSTANCE.mouseClick(mouseX, mouseY, event.getButton(), event.getAction());
                event.setCanceled(true);
            }
        });

        MinecraftForge.EVENT_BUS.addListener((final ScreenEvent.Opening event) -> KeyMappingUtil.restoreAll());

        MinecraftForge.EVENT_BUS.addListener(SearchTreeManager::onPlayerJoin);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, KBClientConfig.SPEC, ModKeyBindBundles.MOD_ID + "-client.toml");
    }
}

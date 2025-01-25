package com.matyrobbrt.keybindbundles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KeyBindBundleManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PATH = FMLPaths.GAMEDIR.get().resolve("keybind_bundles.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static List<KeyBindBundle> keybinds;
    private static List<RadialKeyMapping> keyMappings;

    public static void load() {
        keybinds = new ArrayList<>();
        keyMappings = new ArrayList<>();

        try (var is = Files.newBufferedReader(PATH)) {
            var element = GSON.fromJson(is, JsonArray.class);
            keybinds.addAll(KeyBindBundle.LIST_CODEC.decode(JsonOps.INSTANCE, element)
                    .getOrThrow(false, LOGGER::error).getFirst());

            for (int i = 0; i < keybinds.size(); i++) {
                keyMappings.add(keybinds.get(i).createMapping());
            }

            var options = Minecraft.getInstance().options;
            for (int i = 0; i < options.keyMappings.length; i++) {
                if (options.keyMappings[i] == ModKeyBindBundlesClient.OPEN_SCREEN_MAPPING) {
                    options.keyMappings = ArrayUtils.insert(i + 1, options.keyMappings, keyMappings.toArray(KeyMapping[]::new));
                    break;
                }
            }
        } catch (NoSuchFileException ignore) {

        } catch (IOException ex) {
            LOGGER.error("Error reading file from {}: ", PATH, ex);
        }
    }

    private static KeyMapping getLastKeyMapping() {
        return keyMappings.isEmpty() ? ModKeyBindBundlesClient.OPEN_SCREEN_MAPPING : keyMappings.get(keyMappings.size() - 1);
    }

    public static KeyMapping add(KeyBindBundle bind) {
        keybinds.add(bind);

        var options = Minecraft.getInstance().options;
        var compareKeybind = getLastKeyMapping();
        var mapping = bind.createMapping();
        keyMappings.add(mapping);
        for (int i = 0; i < options.keyMappings.length; i++) {
            if (options.keyMappings[i] == compareKeybind) {
                options.keyMappings = ArrayUtils.insert(i + 1, options.keyMappings, mapping);
                break;
            }
        }

        write();
        return mapping;
    }

    public static void delete(KeyBindBundle bind) {
        var idx = keybinds.indexOf(bind);
        if (idx >= 0) {
            keybinds.remove(idx);
            var map = keyMappings.remove(idx);

            if (map != null) {
                var options = Minecraft.getInstance().options;

                options.keyMappings = ArrayUtils.removeElement(options.keyMappings, map);
                map.kbb$unregister();
            }

            write();
        }
    }

    public static void write() {
        try {
            var out = KeyBindBundle.LIST_CODEC
                    .encodeStart(JsonOps.INSTANCE, keybinds)
                    .getOrThrow(false, LOGGER::error);
            Files.writeString(PATH, GSON.toJson(out));
        } catch (IOException ex) {
            LOGGER.error("Error writing to file {}: ", PATH, ex);
        }
    }

    public static List<RadialKeyMapping> getKeys() {
        return keyMappings;
    }

    public static class RadialKeyMapping extends PriorityKeyMapping {
        public final KeyBindBundle bind;
        private final Component name;
        public RadialKeyMapping(String name, int keyCode, String category, KeyBindBundle bind) {
            super(name, keyCode, category);
            this.bind = bind;
            this.name = Component.translatable("key.keybindbundles.bundle", Component.literal(bind.name).withStyle(ChatFormatting.GOLD));
        }

        @Override
        public Component kbb$getNameOverride() {
            return name;
        }

        private KeyMapping currentlyPressing;

        @Override
        public void setDown(boolean value) {
            if (this.isDown()) {
                if (value) {
                    click();
                } else {
                    onRelease();
                    super.setDown(false);
                }
            } else if (!this.isDown() && value) {
                onClick();
                super.setDown(true);
            }
        }

        private void onClick() {
            if (!opensRadial()) {
                var entry = bind.getBookmarked();
                if (entry != null) {
                    var key = KeyMappingUtil.getByName(entry.key());
                    if (key != null) {
                        setAndPress(key);
                        return;
                    }
                }
            }

            KeybindSelectionOverlay.INSTANCE.open(this);
            Minecraft.getInstance().mouseHandler.releaseMouse();
        }

        public void setAndPress(KeyMapping mapping) {
            currentlyPressing = mapping;
            KeyMappingUtil.press(mapping);
            click();
        }

        private void click() {
            if (currentlyPressing != null) {
                KeyMappingUtil.click(currentlyPressing);
            }
        }

        private void onRelease() {
            if (currentlyPressing != null) {
                KeyMappingUtil.release(currentlyPressing);
                currentlyPressing = null;
            }

            if (KeybindSelectionOverlay.INSTANCE.getDisplayedMapping() == this) {
                KeybindSelectionOverlay.INSTANCE.close();

                var mouse = Minecraft.getInstance().mouseHandler;
                if (!mouse.isMouseGrabbed() && Minecraft.getInstance().screen == null) {
                    mouse.grabMouse();
                }
            }
        }

        private boolean opensRadial() {
            return ModKeyBindBundlesClient.OPEN_RADIAL_MENU_MAPPING.isDown();
        }

        @Override
        public int compareTo(KeyMapping map) {
            if (map == ModKeyBindBundlesClient.OPEN_SCREEN_MAPPING || map == ModKeyBindBundlesClient.OPEN_RADIAL_MENU_MAPPING) return 1;
            return super.compareTo(map);
        }
    }
}

package com.matyrobbrt.keybindbundles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KeyBindBundle {
    public static final Codec<KeyBindBundle> CODEC = RecordCodecBuilder.create(in -> in.group(
            UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(k -> k.id),
            Codec.STRING.fieldOf("name").forGetter(k -> k.name),
            KeyEntry.CODEC.listOf().fieldOf("entries").forGetter(k -> k.entries),
            Codec.INT.optionalFieldOf("bookmark", -1).forGetter(k -> k.bookmark)
    ).apply(in, KeyBindBundle::new));

    public static final Codec<List<KeyBindBundle>> LIST_CODEC = CODEC.listOf();

    private final UUID id;
    public final String name;
    private final List<KeyEntry> entries;
    private int bookmark;

    public KeyBindBundle(UUID id, String name, List<KeyEntry> entries, int bookmark) {
        this.id = id;
        this.name = name;
        this.entries = new ArrayList<>(entries);
        this.bookmark = bookmark;
    }

    @Nullable
    public KeyEntry getBookmarked() {
        return bookmark < 0 || bookmark >= entries.size() ? null : entries.get(bookmark);
    }

    public int getBookmark() {
        return bookmark;
    }

    public List<KeyEntry> getEntries() {
        return entries;
    }

    public void setBookmark(int bookmark) {
        this.bookmark = bookmark;
    }

    public KeyBindBundleManager.RadialKeyMapping createMapping() {
        return new KeyBindBundleManager.RadialKeyMapping(
                "key.keybindbundles.bundle_" + id.toString(),
                GLFW.GLFW_KEY_UNKNOWN,
                "category.keybindbundles",
                this
        );
    }

    public record KeyEntry(String key, String title, ItemStack icon) {
        public static final Codec<KeyEntry> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.STRING.fieldOf("key").forGetter(KeyEntry::key),
                Codec.STRING.fieldOf("title").forGetter(KeyEntry::title),
                ItemStack.CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(KeyEntry::icon)
        ).apply(in, KeyEntry::new));
    }
}

package com.matyrobbrt.keybindbundles.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.CreativeModeTabSearchRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

import java.util.stream.Stream;

public class SearchTreeManager {
    private static SearchTree<ItemStack> basicSearch;

    public static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (basicSearch != null) basicSearch = null;
    }

    public static SearchTree<ItemStack> getSearchTree() {
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            var registries = conn.registryAccess();
            var enabledFeatures = conn.enabledFeatures();

            // mimic CreativeModeInventoryScreen
            CreativeModeTabs.tryRebuildTabContents(enabledFeatures, Minecraft.getInstance().player.canUseGameMasterBlocks() && Minecraft.getInstance().options.operatorItemsTab().get(), registries);

            return Minecraft.getInstance().getSearchTree(CreativeModeTabSearchRegistry.getNameSearchKey(CreativeModeTabs.searchTab()));
        } else {
            if (basicSearch == null) basicSearch = new MappedSearchTree<>(new IdSearchTree<>(i -> Stream.of(i.builtInRegistryHolder().unwrapKey().orElseThrow().location()), new RegistryBackedList<>(BuiltInRegistries.ITEM, Item.class)), Item::getDefaultInstance);
            return basicSearch;
        }
    }
}

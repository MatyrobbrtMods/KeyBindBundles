package com.matyrobbrt.keybindbundles.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SearchTreeManager {
    private static SearchTree<ItemStack> basicSearch;

    public static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (basicSearch != null) basicSearch = null;
    }

    public static SearchTree<ItemStack> getSearchTree() {
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            var searchTrees = conn.searchTrees();
            var registries = conn.registryAccess();
            var enabledFeatures = conn.enabledFeatures();

            // mimic CreativeModeInventoryScreen
            if (CreativeModeTabs.tryRebuildTabContents(enabledFeatures, Minecraft.getInstance().player.canUseGameMasterBlocks() && Minecraft.getInstance().options.operatorItemsTab().get(), registries)) {
                CreativeModeTabs.allTabs().stream().filter(CreativeModeTab::hasSearchBar).forEach(tab -> {
                    List<ItemStack> list = List.copyOf(tab.getDisplayItems());
                    searchTrees.updateCreativeTooltips(registries, list, Objects.requireNonNull(CreativeModeTabSearchRegistry.getNameSearchKey(tab)));
                    searchTrees.updateCreativeTags(list, Objects.requireNonNull(CreativeModeTabSearchRegistry.getTagSearchKey(tab)));
                });
            }

            return searchTrees.creativeNameSearch();
        } else {
            if (basicSearch == null) basicSearch = new MappedSearchTree<>(new IdSearchTree<>(i -> Stream.of(i.builtInRegistryHolder().getKey().location()), new RegistryBackedList<>(BuiltInRegistries.ITEM, Item.class)), Item::getDefaultInstance);
            return basicSearch;
        }
    }
}

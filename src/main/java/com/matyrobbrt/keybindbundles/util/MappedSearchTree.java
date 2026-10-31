package com.matyrobbrt.keybindbundles.util;

import net.minecraft.client.searchtree.SearchTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record MappedSearchTree<T, Z>(SearchTree<T> tree, Function<T, Z> mapper) implements SearchTree<Z> {
    @Override
    public List<Z> search(String query) {
        var searched = tree.search(query);
        var newList = new ArrayList<Z>(searched.size());
        for (int i = 0; i < searched.size(); i++) {
            newList.add(mapper.apply(searched.get(i)));
        }
        return newList;
    }
}

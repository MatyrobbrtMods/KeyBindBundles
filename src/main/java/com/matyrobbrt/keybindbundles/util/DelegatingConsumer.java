package com.matyrobbrt.keybindbundles.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DelegatingConsumer<T> implements Consumer<T> {
    private final List<Consumer<T>> list = new ArrayList<>();

    public void add(Consumer<T> cons) {
        list.add(cons);
    }

    @Override
    public void accept(T t) {
        for (Consumer<T> tConsumer : list) {
            tConsumer.accept(t);
        }
    }
}

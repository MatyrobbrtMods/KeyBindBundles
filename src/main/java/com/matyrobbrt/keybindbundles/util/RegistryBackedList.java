package com.matyrobbrt.keybindbundles.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public record RegistryBackedList<T>(Registry<T> registry, Class<T> type) implements List<T> {
    @Override
    public int size() {
        return registry.size();
    }

    @Override
    public boolean isEmpty() {
        return registry.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return type.isInstance(o) && registry.wrapAsHolder((T)o).kind() == Holder.Kind.REFERENCE;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return registry.stream().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        // TODO - is this ok?
        return (T1[]) registry.stream().toArray();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return registry.byId(index);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return type.isInstance(o) ? registry.getId((T) o) : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return type.isInstance(o) ? registry.getId((T) o) : -1;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return registry.stream().toList().listIterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return registry.stream().toList().listIterator();
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return registry.stream().toList().subList(fromIndex, toIndex);
    }
}

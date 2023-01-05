package org.ru.vortex.modules.history.components;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class CirculatingArray<T> {

    public final T[] array;
    public final int staticSize;
    public int deletePointer = 0;
    public Consumer<Integer> onRemove = index -> {};

    public CirculatingArray(T[] array) {
        this.array = array;
        this.staticSize = array.length;
    }

    public void onRemove(Consumer<Integer> action) {
        this.onRemove = action;
    }

    public T get(int index) {
        return array[index];
    }

    public int size() {
        return array[staticSize - 1] != null ? staticSize : deletePointer;
    }

    public boolean add(T element) {
        if (deletePointer >= staticSize - 1) deletePointer = 0;

        if (array[deletePointer] != null) onRemove.accept(deletePointer);
        array[deletePointer] = element;
        deletePointer++;
        return true;
    }

    public boolean addAll(Collection<? extends T> collection) {
        collection.forEach(this::add);
        return true;
    }

    public boolean contains(Function<T, Boolean> elementFinder) {
        return indexOf(elementFinder) != -1;
    }

    public boolean contains(T element) {
        return indexOf(element) != -1;
    }

    public int indexOf(Function<T, Boolean> elementFinder) {
        for (int i = 0; i < staticSize; i++) {
            T element = array[i];

            if (array[i] == null) return -1;
            if (elementFinder.apply(element)) return i;
        }

        return -1;
    }

    public int indexOf(T element) {
        for (int i = 0; i < staticSize; i++) {
            if (array[i] == null) return -1;
            if (array[i].equals(element)) return i;
        }

        return -1;
    }
}

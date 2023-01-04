package org.ru.vortex.modules.history.components;

import java.util.Collection;

public class LongCirculatingArray {

    public long[] array;
    public int staticSize;
    public int deletePointer = 0;

    public LongCirculatingArray(long[] array) {
        this.array = array;
        this.staticSize = array.length;
    }

    public synchronized long get(int index) {
        return array[index];
    }

    public synchronized void remove(int index) {
        array[index] = 0;
    }

    public int size() {
        return array[staticSize - 1] == 0 ? deletePointer : staticSize;
    }

    public boolean add(long element) {
        if (deletePointer >= staticSize - 1) deletePointer = 0;

        array[deletePointer] = element;
        deletePointer++;
        return true;
    }

    public boolean addAll(Collection<Long> collection) {
        collection.forEach(this::add);
        return true;
    }

    public boolean contains(long element) {
        return indexOf(element) != -1;
    }

    public int indexOf(long element) {
        for (int i = 0; i < staticSize; i++) {
            if (array[i] == 0) return -1;
            if (array[i] == element) return i;
        }

        return -1;
    }
}

package org.ru.vortex.modules.history.components;

import arc.math.Mathf;

public class LongPackager {

    public long packaged = 0;
    public byte readShift = 0;

    public LongPackager(long initial) {
        packaged = initial;
    }

    public void write(int reservedLength, long data) {
        packaged <<= reservedLength;
        packaged |= data;
    }

    public long readBites(byte length) {
        readShift += length;

        return readBites((byte) (readShift - length), length);
    }

    public long readBites(byte shift, byte length) {
        return (packaged & (((long) Mathf.pow(2, length) - 1) << shift)) >> shift;
    }

    public long read() {
        return packaged;
    }
}

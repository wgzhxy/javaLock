package org.wanggz.nio.nioexam;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public class FastByteArrayOutputStream extends OutputStream {

    private static final int DEFAULT_BLOCK_SIZE = 8192;

    private LinkedList<byte[]> buffers;
    private byte buffer[];
    private int index;
    private int size;
    private int blockSize;
    private boolean closed;

    public FastByteArrayOutputStream() {
        this(DEFAULT_BLOCK_SIZE);
    }

    public FastByteArrayOutputStream(int blockSize) {
        buffer = new byte[this.blockSize = blockSize];
    }

    public void writeTo(OutputStream out) throws IOException {
        if (buffers != null) {
            for (byte[] bytes : buffers) {
                out.write(bytes, 0, blockSize);
            }
        }
        out.write(buffer, 0, index);
    }

    public int size() {
        return size + index;
    }

    public byte[] toByteArray() {
        byte data[] = new byte[size()];
        int position = 0;
        if (buffers != null) {
            for (byte[] bytes : buffers) {
                System.arraycopy(bytes, 0, data, position, blockSize);
                position += blockSize;
            }
        }
        System.arraycopy(buffer, 0, data, position, index);
        return data;
    }

    public String toString() {
        return new String(toByteArray());
    }

    protected void addBuffer() {
        if (buffers == null) {
            buffers = new LinkedList<byte[]>();
        }
        buffers.addLast(buffer);
        buffer = new byte[blockSize];
        size += index;
        index = 0;
    }

    public void write(int datum) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        if (index == blockSize) {
            addBuffer();
        }
        buffer[index++] = (byte) datum;
    }

    public void write(byte data[], int offset, int length) {
        if (data == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || offset + length > data.length || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (closed) {
            throw new IllegalStateException("Stream closed");
        }
        if (index + length > blockSize) {
            do {
                if (index == blockSize) {
                    addBuffer();
                }
                int copyLength = blockSize - index;
                if (length < copyLength) {
                    copyLength = length;
                }
                System.arraycopy(data, offset, buffer, index, copyLength);
                offset += copyLength;
                index += copyLength;
                length -= copyLength;
            } while (length > 0);
        } else {
            System.arraycopy(data, offset, buffer, index, length);
            index += length;
        }
    }

    public void close() {
        closed = true;
    }

    public void reset() {
        buffers.clear();
        index = 0;
        size = 0;
    }

}
package swcnoops.server.datasource.buffers;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Not thread safe
 * @param <T>
 */
public class FixedRingBuffer<T> implements RingBuffer<T> {
    final private T[] buffer;
    private int head = -1;
    private int tail = 0;
    private int numberOfObjects = 0;

    public FixedRingBuffer(Class<T> clazz, int capacity) {
        this.buffer = createBuffer(clazz, capacity);
    }

    private T[] createBuffer(Class<T> clazz, int capacity) {
        return (T[]) Array.newInstance(clazz, capacity);
    }

    @Override
    public int getCapacity() {
        return this.buffer.length;
    }

    @Override
    public int getTail() {
        return this.tail;
    }

    @Override
    public int getHead() {
        return this.head;
    }

    @Override
    public int getNumberOfObjects() {
        return this.numberOfObjects;
    }

    @Override
    public void add(T object) {
        this.head++;

        // see if we are at end of ring to loop round
        if (this.head == this.buffer.length) {
            this.head = 0;
        }

        // if on the tail then move the tail
        if (this.head == this.tail && this.numberOfObjects == this.buffer.length) {
            this.tail++;

            if (this.tail == this.buffer.length)
                this.tail = 0;
        }

        this.buffer[this.head] = object;

        if (this.numberOfObjects < this.buffer.length)
            this.numberOfObjects++;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int atObject = 0;

            @Override
            public boolean hasNext() {
                return atObject < numberOfObjects;
            }

            @Override
            public T next() {
                int atIndex = tail + atObject;
                atObject++;
                if (atIndex >= buffer.length)
                    atIndex -= buffer.length;
                return buffer[atIndex];
            }
        };
    }
}

package swcnoops.server.datasource.buffers;

public interface RingBuffer<T> extends Iterable<T> {
    void add(T object);

    int getTail();

    int getHead();

    int getNumberOfObjects();

    int getCapacity();

    void clear();
}

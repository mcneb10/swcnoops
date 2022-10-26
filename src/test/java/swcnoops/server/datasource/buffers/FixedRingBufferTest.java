package swcnoops.server.datasource.buffers;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class FixedRingBufferTest {
    @Test
    public void basicTest() {
        RingBuffer<Integer> ringBuffer = new FixedRingBuffer<>(Integer.class, 5);
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        assertRingIterator(ringBuffer, 1, 3);
        ringBuffer.add(4);
        ringBuffer.add(5);
        assertRingIterator(ringBuffer, 1, 5);
        assertEquals(0, ringBuffer.getTail());
        assertEquals(4, ringBuffer.getHead());
        assertEquals(5, ringBuffer.getNumberOfObjects());
        ringBuffer.add(6);
        assertEquals(1, ringBuffer.getTail());
        assertEquals(0, ringBuffer.getHead());
        assertEquals(5, ringBuffer.getNumberOfObjects());
        assertRingIterator(ringBuffer, 2, 6);
        ringBuffer.add(7);
        ringBuffer.add(8);
        assertRingIterator(ringBuffer, 4, 8);
        ringBuffer.add(9);
        ringBuffer.add(10);
        assertEquals(0, ringBuffer.getTail());
        assertEquals(4, ringBuffer.getHead());
        assertEquals(5, ringBuffer.getNumberOfObjects());
        assertRingIterator(ringBuffer, 6, 10);
    }

    private void assertRingIterator(RingBuffer<Integer> ringBuffer, int startValue, int endValue) {
        Iterator<Integer> iterator = ringBuffer.iterator();
        int count = 0;
        boolean endFound = false;
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            assertEquals(startValue + count, value.intValue());

            if ((count + startValue) == endValue)
                endFound = true;

            count++;
        }

        assertEquals(count, ringBuffer.getNumberOfObjects());
        assertEquals(true, endFound);
    }
}

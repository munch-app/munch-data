package munch.data.extended;

import com.google.common.collect.ImmutableSet;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by: Fuxing
 * Date: 6/3/2018
 * Time: 1:44 AM
 * Project: munch-data
 */
public class ExtendedDataSync<T extends ExtendedData> {

    private final Duration editSleep;
    private final ExtendedDataClient<T> dataClient;

    /**
     * @param editSleep      sleep, edit sleep interval, (does not effect list operation)
     * @param dataClient     data client for list, put & delete
     */
    public ExtendedDataSync(Duration editSleep, ExtendedDataClient<T> dataClient) {
        this.editSleep = editSleep;
        this.dataClient = dataClient;
    }

    /**
     * @param placeId  placeId
     * @param incoming data iterator, must not contains any null
     */
    public void sync(String placeId, Iterator<T> incoming) {
        Queue incomingQueue = new Queue();
        Queue existingQueue = new Queue();

        Iterator<T> existing = dataClient.iterator(placeId);
        while (existing.hasNext() && incoming.hasNext()) {
            T in = incoming.next();
            T exist = existing.next();

            if (in.getSortKey().equals(exist.getSortKey())) {
                // Both Match, Apply Straight Away
                apply(placeId, in, exist);
            } else {
                // Save both to queue
                incomingQueue.put(in);
                existingQueue.put(exist);
            }

            // Compare both queue is see if any is matched
            compare(placeId, incomingQueue, existingQueue);
        }

        // Possible Improvement: to continue iterating of the iterator and check

        // Extract data from both ends
        incoming.forEachRemaining(incomingQueue::put);
        existing.forEachRemaining(existingQueue::put);

        // Compare both last time and clean up that that can be applied
        compare(placeId, incomingQueue, existingQueue);

        // All leftover existing get deleted
        existingQueue.forEach((s, data) -> delete(placeId, s));

        // All leftover incoming get saved
        incomingQueue.forEach((s, data) -> put(placeId, data));
    }

    private void apply(String placeId, T in, T exist) {
        if (in.equals(exist)) return;

        // If changed, override
        put(placeId, in);
    }

    private void compare(String placeId, Map<String, T> incomingQueue, Map<String, T> existingQueue) {
        for (String sortKey : ImmutableSet.copyOf(incomingQueue.keySet())) {
            T exist = existingQueue.get(sortKey);

            // Found on both sides, apply and delete from queue
            if (exist != null) {
                T in = incomingQueue.get(sortKey);
                apply(placeId, in, exist);
                incomingQueue.remove(sortKey);
                existingQueue.remove(sortKey);
            }
        }
    }

    private void put(String placeId, T data) {
        sleep();
        dataClient.put(placeId, data);
    }

    private void delete(String placeId, String sortKey) {
        sleep();
        dataClient.delete(placeId, sortKey);
    }

    private void sleep() {
        try {
            Thread.sleep(editSleep.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper Queue
     */
    private class Queue extends HashMap<String, T> {
        public void put(T data) {
            put(data.getSortKey(), data);
        }
    }
}

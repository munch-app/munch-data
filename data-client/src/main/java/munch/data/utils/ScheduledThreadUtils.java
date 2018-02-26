package munch.data.utils;

import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 7:02 PM
 * Project: munch-data
 */
@Singleton
public final class ScheduledThreadUtils {
    private static ScheduledExecutorService executorService;

    public static void init(int size) {
        executorService = Executors.newScheduledThreadPool(size);

        // In case user didn't shut it down
        Runtime.getRuntime().addShutdownHook(new Thread(ScheduledThreadUtils::shutdown));
    }

    public static void schedule(Runnable runnable, long period, TimeUnit unit) {
        schedule(runnable, period, period, unit);
    }

    public synchronized static void schedule(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        if (executorService == null) {
            init(1);
        }
        executorService.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }

    public static void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

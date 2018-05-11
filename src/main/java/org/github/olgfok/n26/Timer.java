package org.github.olgfok.n26;

import javax.annotation.PreDestroy;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer {

    private ScheduledExecutorService executorService;

    public Timer(ScheduledExecutorService scheduledExecutorService) {
        this.executorService = scheduledExecutorService;
    }


    /**
     * Start timer
     * @param task - task to be executed
     * @param frequencyMilliseconds - time in milliseconds, how often it should be executed
     */
    public void start(TimerTask task, int frequencyMilliseconds) {
        executorService.scheduleAtFixedRate(task, 0, frequencyMilliseconds, TimeUnit.MILLISECONDS);

    }


    @PreDestroy
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

    }


}

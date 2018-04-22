package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for gathering statistics
 */
@Service
public class StatisticsService {

    public static final int TIME_LIMIT_MILLIS = 60000;
  /*  this defines how often statistics is updated
    1000 - once per second
    100 - once per 100 milliseconds
    the less this number is, the more precise is result
    the number also influence the size of map, where we store statistics
    with smaller number, the map is larger*/
    private static final int PRECISION = 1000;
    // private static final int TIMER_INTERVAL = 1000/PRECISION;
    public static final int TIME_LIMIT = TIME_LIMIT_MILLIS / PRECISION;

    private volatile int timer = 0;

    private final ConcurrentHashMap<Integer, Statistics> statisticsMap = new ConcurrentHashMap();
    private ScheduledExecutorService timerExecutor;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    public void addTransaction(Transaction transaction) {
        double amount = transaction.getAmount();

        long timestamp = transaction.getTimestamp();
        long millis = calcTimeDifference(timestamp);
        int timelinePoint = findTimelinePoint(millis);
        //no point to recalc statitiscs for this
        //as it immediately expires
        //  logger.debug("millis " + millis);

        if (millis >= TIME_LIMIT_MILLIS) return;
        // logger.debug("new transaction " + transaction + " timelinePoint " + timelinePoint);

        synchronized (statisticsMap) {
            // logger.debug("timer value " + timer);

            int bucketIndex = (timelinePoint <= timer ? TIME_LIMIT - timer + timelinePoint + 1 :
                    timelinePoint - timer) % TIME_LIMIT;
            logger.debug("timelinePoint = " + timelinePoint + " timer = " + timer + "bucket index " + bucketIndex);

            Statistics statistics = statisticsMap.get(bucketIndex);
            if (statistics == null) {
                statistics = new Statistics(amount, amount, amount,
                        1L);
            } else {
                statistics = recalcStatistics(statistics, transaction);
            }
            //if difference is still less than 60 seconds, update statistics
            millis = calcTimeDifference(timestamp);
            if (millis < TIME_LIMIT_MILLIS) {
                statisticsMap.put(bucketIndex, statistics);
            }


        }
        logger.debug("statisticsMap updated " + statisticsMap);


    }

    private long calcTimeDifference(long timestamp) {
        return System.currentTimeMillis() - timestamp;
    }

    private Statistics recalcStatistics(Statistics cur, Transaction t) {
        double sum = cur.getSum() + t.getAmount();
        long count = cur.getCount() + 1;
        double max = cur.getMax();
        if (max < t.getAmount()) {
            max = t.getAmount();
        }
        Double min = cur.getMin();
        if (min == null || min > t.getAmount()) {
            min = t.getAmount();
        }

        return new Statistics(sum,
                min, max, count);

    }

    @PostConstruct
    public void start() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(new StatisticsTimerTask(), 0, PRECISION, TimeUnit.MILLISECONDS);


    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        timerExecutor.shutdown();
        timerExecutor.awaitTermination(5, TimeUnit.SECONDS);


    }

    private class StatisticsTimerTask implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(StatisticsTimerTask.class);

        @Override
        public void run() {
            int deleteIndex = 0;
            synchronized (statisticsMap) {
                deleteIndex = TIME_LIMIT - timer;
                /*if (statisticsMap.get(deleteIndex)!=null) {
                }*/
                Statistics remove = statisticsMap.remove(deleteIndex);
                if (remove != null) {
                    logger.debug("statisticsMap removed " + deleteIndex);
                }
                timer += 1;


                //timer++;
                if (timer == TIME_LIMIT) {
                    timer = 0;
                }
            }

        }

    }

    public Statistics getStatistics() {

        double sum = 0;
        Double min = null;
        double max = 0;
        long count = 0;

        Enumeration<Statistics> elements = statisticsMap.elements();
        while (elements.hasMoreElements()) {
            Statistics s = elements.nextElement();
            sum = sum + s.getSum();
            if (min == null || min > s.getMin()) {
                min = s.getMin();
            }
            if (max < s.getMax()) {
                max = s.getMax();
            }
            count = count + s.getCount();
        }

        if (min == null) {
            min = 0d;
        }
        return new Statistics(sum, min, max, count);

        //
    }

    private int findTimelinePoint(long secondsAgo) {
        return (int) Math.round((double) secondsAgo / PRECISION) % (TIME_LIMIT + 1);
    }
}

package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for collecting statistics
 */
@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    private static final int TIME_LIMIT_MILLIS = 60000;

    public static final int TIME_LIMIT = 60;

    private volatile int secondsCounter = 0;

    /**
     * Stores statistics for the last 60 seconds
     * Statistics for each second correspond to a separate bucket
     */
    private final ConcurrentHashMap<Integer, Statistics> statisticsMap = new ConcurrentHashMap<>();

    @Autowired
    private Timer timer;

    private StatisticsTimerTask task = new StatisticsTimerTask();

    @PostConstruct
    public void start() {
        //executing task every second
        timer.start(task, 1000);
    }

    public StatisticsTimerTask getTask() {
        return task;
    }

    public void addTransaction(Transaction transaction) {
        final double amount = transaction.getAmount();
        final long timestamp = transaction.getTimestamp();
        long millis = calcTimeDifference(timestamp);
        int timelinePoint = findTimelinePoint(millis);
        //no point to recalc statitics for this
        //as it immediately expires
        if (millis >= TIME_LIMIT_MILLIS) return;

        synchronized (statisticsMap) {
            int bucketIndex = getBucketIndex(timelinePoint);
            Statistics statistics = statisticsMap.get(bucketIndex);
            if (statistics == null) {
                statistics = new Statistics(amount, amount, amount,
                        1L);
            } else {
                statistics = recalcStatistics(statistics, transaction);
            }
            //if the difference is still less than 60 seconds, update statistics
            millis = calcTimeDifference(timestamp);
            if (millis < TIME_LIMIT_MILLIS) {
                statisticsMap.put(bucketIndex, statistics);
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("statisticsMap updated " + statisticsMap);

    }

    //calculating in which bucket to put current statistics
    private int getBucketIndex(int timelinePoint) {
        return ((timelinePoint <= secondsCounter) ?
                (TIME_LIMIT - secondsCounter + timelinePoint + 1) :
                (timelinePoint - secondsCounter)) % TIME_LIMIT;
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

    /**
     * The task should be executed every second
     * Statistics older than 60 second ago should be removed
     */
    public class StatisticsTimerTask extends TimerTask {
        private final Logger logger = LoggerFactory.getLogger(StatisticsTimerTask.class);

        @Override
        public void run() {
            synchronized (statisticsMap) {
                int deleteIndex = TIME_LIMIT - secondsCounter;
                Statistics remove = statisticsMap.remove(deleteIndex);

                if (logger.isDebugEnabled() && remove != null) {
                    logger.debug("statisticsMap removed " + deleteIndex);
                }
                secondsCounter++;

                //calculating up to 60
                if (secondsCounter == TIME_LIMIT) {
                    secondsCounter = 0;
                }
            }
        }
    }

    /**
     * Calculate statistics for the  last 60 seconds
     * @return statistics
     */
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

    }

    private int findTimelinePoint(long secondsAgo) {
        return (int) Math.round((double) secondsAgo / 1000) % (TIME_LIMIT + 1);
    }


}

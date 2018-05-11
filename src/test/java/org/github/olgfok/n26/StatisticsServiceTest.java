package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;
    private static final int NUMBER_OF_THREADS = 10;
    private static final int NUMBER_OF_TRANSACTIONS = 100;

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    @Test
    public void testStatistics() {
        final double amount = 100;
        Statistics statistics = new Statistics(amount * NUMBER_OF_TRANSACTIONS / 2, amount, amount, NUMBER_OF_TRANSACTIONS / 2);

        List<Future> es = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            int millisAgo = 57790;
            if (i >= NUMBER_OF_TRANSACTIONS / 2) {
                millisAgo = 10000;
            }
            long timestamp = System.currentTimeMillis() - millisAgo;

            Future<?> submit = executorService.submit(() -> statisticsService.addTransaction(new Transaction(
                    timestamp,
                    amount)));
            es.add(submit);
        }

        for (Future f : es) {
            while (!f.isDone()) {
            }
        }

        //let some statistics expire
        //imitate as though 4 seconds have passed
        for (int i = 0; i < 4; i++) {
            statisticsService.getTask().run();
        }
        Statistics statisticsResult = statisticsService.getStatistics();

        assertEquals(statistics, statisticsResult);
    }

    @Test
    public void testStatistics_Expiration() {
        final double amount = 100;
        Statistics statistics = new Statistics(amount * NUMBER_OF_TRANSACTIONS, amount, amount, NUMBER_OF_TRANSACTIONS);

        List<Future> es = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            long timestamp = System.currentTimeMillis() - 57600;
            Future<?> submit = executorService.submit(() -> statisticsService.addTransaction(new Transaction(
                    timestamp,
                    amount)));
            es.add(submit);
        }

        for (Future f : es) {
            while (!f.isDone()) {
            }
        }

        Statistics result = statisticsService.getStatistics();
        assertEquals(statistics, result);

        //let some statistics expire
        //imitate as though 4 seconds have passed
        for (int i = 0; i < 4; i++) {
            statisticsService.getTask().run();
        }
        statistics = new Statistics(0d, 0d, 0d, 0);
        Statistics resultExpired = statisticsService.getStatistics();
        assertEquals(statistics, resultExpired);

    }
}

package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.Transaction;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;
    private static final int NUMBER_OF_THREADS = 10;
    private static final int NUMBER_OF_TRANSACTIONS = 100;

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    @Test
    public void testStatistics() throws InterruptedException, ExecutionException {
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
        Thread.sleep(4000);
        Statistics statisticsResult = statisticsService.getStatistics();

        assertEquals(statistics, statisticsResult);
    }

    @Test
    public void testStatistics_Expiration() throws InterruptedException {
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

        Thread.sleep(4000);
        statistics = new Statistics(0d, 0d, 0d, 0);
        Statistics resultExpired = statisticsService.getStatistics();
        assertEquals(statistics, resultExpired);

    }
}

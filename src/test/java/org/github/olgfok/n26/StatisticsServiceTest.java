package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.Transaction;
import org.github.olgfok.n26.dto.TransactionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;
    private static final int NUMBER_OF_THREADS = 10;
    private static final int NUMBER_OF_TRANSACTIONS = 1;

    ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    @Test
    public void testStatistics() throws InterruptedException, ExecutionException {
        final double amount = 100;
        //let timer to start counting
        // Thread.sleep(5000);

        Statistics statistics = new Statistics(amount * NUMBER_OF_TRANSACTIONS, amount, amount, NUMBER_OF_TRANSACTIONS);

        List<Future> es = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            long timestamp = System.currentTimeMillis() - 55790;
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
        Future<Statistics> statistics1 = executorService.submit(() -> statisticsService.getStatistics());
        Future<Statistics> statistics2 = executorService.submit(() -> statisticsService.getStatistics());

        while (!statistics1.isDone()) ;

        Statistics result = statistics1.get();
        assertEquals(statistics, result);
        Statistics result2 = statistics2.get();
        assertEquals(statistics, result2);
    }

    @Test
    public void testStatistics_Expiration() throws InterruptedException, ExecutionException {
        final double amount = 100;
        //let timer to start counting
        // Thread.sleep(5000);

        Statistics statistics = new Statistics(amount * NUMBER_OF_TRANSACTIONS, amount, amount, NUMBER_OF_TRANSACTIONS);

        List<Future> es = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            long timestamp = System.currentTimeMillis() - 59600;
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


        Thread.sleep(7000);
        statistics = new Statistics(0d, 0d, 0d, 0);
        Statistics resultExpired = statisticsService.getStatistics();
        assertEquals(statistics, resultExpired);


    }
}

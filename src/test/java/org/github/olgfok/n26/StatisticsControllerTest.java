package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.TransactionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@WebMvcTest(StatisticsController.class)
public class StatisticsControllerTest {

    @MockBean
    private StatisticsService statisticsService;

    @Autowired
    private StatisticsController statisticsController;

    @Test
    public void transactionsTest_201() {
        TransactionRequest transaction = new TransactionRequest();
        transaction.setAmount(100);
        transaction.setTimestamp(System.currentTimeMillis());
        Integer response = statisticsController.transactions(transaction);
        assertEquals(HttpStatus.CREATED.value(), response.intValue());
    }

    @Test
    public void transactionsTest_204() {
        TransactionRequest transaction = new TransactionRequest();
        transaction.setAmount(100);
        transaction.setTimestamp(System.currentTimeMillis() - 60001);
        Integer response = statisticsController.transactions(transaction);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.intValue());

    }

    @Test
    public void statisticsTest() {

        Statistics statistics = new Statistics(2300, 300d, 2000d, 2);
        given(statisticsService.getStatistics()).willReturn(statistics);
        Statistics response = statisticsController.statistics();
        assertEquals(statistics, response);


    }
}

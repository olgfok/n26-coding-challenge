package org.github.olgfok.n26;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ScheduledExecutorService;

@TestConfiguration
public class TestConfig {

    @Bean
    public StatisticsService statisticsService() {
        return new StatisticsService();
    }

    @MockBean
    private ScheduledExecutorService executorService;

    @Bean
    public Timer timer() {
        return new Timer(executorService);
    }

}
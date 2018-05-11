package org.github.olgfok.n26;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class StatisticsConfig {

    @Bean
    public ScheduledExecutorService executorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Bean
    public Timer timer() {
        return new Timer(executorService());
    }


}

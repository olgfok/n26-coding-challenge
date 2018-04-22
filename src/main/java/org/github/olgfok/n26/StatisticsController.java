package org.github.olgfok.n26;

import org.github.olgfok.n26.dto.Statistics;
import org.github.olgfok.n26.dto.Transaction;
import org.github.olgfok.n26.dto.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main controller for managing requests
 */
@RestController
public class StatisticsController {
    private Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * @param request containing
     *                amount - transaction amount
     *                timestamp - transaction time in epoch in millis in UTC time zone (this is not current
     *                timestamp)
     * @return Empty body with either 201 or 204
     * 201 - in case of success
     * 204 - if transaction is older than 60 seconds
     */
    @PostMapping("/transactions")
    public Integer transactions(@RequestBody TransactionRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("transactions params: " + request);
        }
        Long now = System.currentTimeMillis();
        Long timestamp = request.getTimestamp();
        long diff = now - timestamp;
        if (diff > StatisticsService.TIME_LIMIT * 1000) {
            return HttpStatus.NO_CONTENT.value();
        } else {
            statisticsService.addTransaction(new Transaction(timestamp, request.getAmount()));
            return HttpStatus.CREATED.value();
        }
    }

    /**
     * @return the statistic based on the transactions which happened in the last 60
     * seconds.
     * - sum is a double specifying the total sum of transaction value in the last 60 seconds
     * - avg is a double specifying the average amount of transaction value in the last 60
     * seconds
     * - max is a double specifying single highest transaction value in the last 60 seconds
     * - min is a double specifying single lowest transaction value in the last 60 seconds
     * - count is a long specifying the total number of transactions happened in the last 60
     * seconds
     */
    @GetMapping("/statistics")
    public Statistics statistics() {
        return statisticsService.getStatistics();
    }


}

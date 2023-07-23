package org.project.lottery.v1.controller;

import lombok.AllArgsConstructor;
import org.project.lottery.v1.dto.CreateLotteryRequest;
import org.project.lottery.v1.dto.IssueTicketRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Lottery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/v1/lottery-load-test")
@AllArgsConstructor
@Validated
public class LoadTestController {

    private static final Logger logger = LoggerFactory.getLogger(LoadTestController.class);

    @Autowired
    private TicketController ticketController;

    @Autowired
    private LotteryController lotteryController;

    @GetMapping("/test")
    public void loadTest() throws InterruptedException, ExecutionException {
        logger.info("Starting load test...");
        String lotteryName = String.format("lottery-min-super-%s", UUID.randomUUID());
        CreateLotteryRequest createLotteryRequest = new CreateLotteryRequest(lotteryName, 1000L);
        ResponseEntity<LotteryResponse> lottery = lotteryController.createLottery(createLotteryRequest);
        ExecutorService executorService = Executors.newWorkStealingPool(100);
        List<Future<ResponseEntity<TicketResponse>>> futures = new ArrayList<>();

        for (int i = 0; i < 1001; i++) {
            // TODO We can trigger direct end points to test load balance and distributed locking.
            IssueTicketRequest request = new IssueTicketRequest(UUID.randomUUID(), lottery.getBody().id());
            Future<ResponseEntity<TicketResponse>> future = executorService.submit(() -> ticketController.issueTicket(request));
            futures.add(future);
        }
        for (Future<ResponseEntity<TicketResponse>> future : futures) {
            ResponseEntity<TicketResponse> responseEntity = future.get();
            logger.info("Ticket issued for user: {}", responseEntity.getBody());
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        logger.info("Load test completed.");
    }
}

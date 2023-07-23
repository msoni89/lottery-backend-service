package org.project.lottery.v2.handlers;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.project.lottery.v1.dto.IssueTicketRequest;
import org.project.lottery.v1.service.ticket.ITicketService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.ServerResponse.ok;

@Component
@AllArgsConstructor
@Slf4j
public class LotteryHandler {
    private final ITicketService lotteryService;
    @SneakyThrows
    public ServerResponse issueTicket(ServerRequest req) {

        var body = req.body(IssueTicketRequest.class);
        log.info(String.format("Inside create method, Request received with body %s", body));

        lotteryService.issueTicket(body);
        return ok().build();
    }
}

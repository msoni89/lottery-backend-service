package org.project.lottery.v2.routes;

import org.project.lottery.v2.handlers.LotteryHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.POST;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Component
public class LotteryRoutes {

    // Functional endpoint - We can also use function routers to build.
    @Bean
    public RouterFunction<ServerResponse> useLotteryRoutes(LotteryHandler lotteryHandler) {
        return route(POST("/api/v2/lottery/ticket"), lotteryHandler::issueTicket);
    }
}

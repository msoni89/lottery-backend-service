package org.project.controllers;

import org.junit.jupiter.api.Test;
import org.project.LotteryApplication;
import org.project.lottery.v1.dto.CreateLotteryRequest;
import org.project.lottery.v1.dto.IssueTicketRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = LotteryApplication.class)
@AutoConfigureMockMvc
public class LotteryControllerTests {
    static {
        GenericContainer redis = new GenericContainer("redis:5.0.3-alpine")
                .withExposedPorts(6379);
        redis.start();
        System.setProperty("spring.redis.host", redis.getHost());
        System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void issueTicket_returns_not_found_error_if_lottery_does_not_exist() throws Exception {

        UUID LotteryId = UUID.randomUUID();
        // Create a request to issue a ticket for a lottery that does not exist.
        IssueTicketRequest request = IssueTicketRequest.builder()
                .userId(UUID.randomUUID())
                .lotteryId(LotteryId)
                .build();

        // Send the request and expect a 404 Not Found response.
        mvc.perform(post("/api/v1/ticket")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(String.format("Lottery with lotteryId %s does not exist", LotteryId))))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")));
    }

    @Test
    public void allocate_1_ticket_and_issue_ticket_return_ok() throws Exception {

        // Create a lottery with 1 ticket
        String lotteryName = "keu_allocate_1_ticket_and_issue_ticket_return_ok";
        MvcResult mvcResult = mvc.perform(post("/api/v1/lottery")
                        .content(objectMapper.writeValueAsString(CreateLotteryRequest.builder().lotteryName(lotteryName).totalLotteryTicketAllotted(1L).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(lotteryName)))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        LotteryResponse lotteryResponse
                = objectMapper.readValue(responseBody, LotteryResponse.class);

        // Issue a ticket for the lottery
        mvc.perform(post("/api/v1/ticket")
                        .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(UUID.randomUUID()).lotteryId(lotteryResponse.id()).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lottery.name", is(lotteryName)))
                .andExpect(jsonPath("$.lottery_number", is(String.format("lottery_number_%d", 1))));
    }

    @Test
    public void testAllotMax_1TicketLottery_TwoUser_Attempt_First_Get_Success_And_Second_Out_Of_Stock_Error()
            throws Exception {

        // Create a lottery with 1 ticket
        String name = "1_user_ticket_lottery";
        MvcResult mvcResult = mvc.perform(post("/api/v1/lottery")
                        .content(objectMapper.writeValueAsString(CreateLotteryRequest.builder().lotteryName(name).totalLotteryTicketAllotted(1L).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(name))).andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        LotteryResponse lotteryResponse
                = objectMapper.readValue(responseBody, LotteryResponse.class);

        // Issue a ticket for the first user
        mvc.perform(post("/api/v1/ticket")
                        .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(UUID.randomUUID()).lotteryId(lotteryResponse.id()).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lottery.name", is(name)))
                .andExpect(jsonPath("$.lottery_number", is(String.format("lottery_number_%d", 1))));

        // Issue a ticket for the second user, expecting an Out-of-Stock error
        mvc.perform(post("/api/v1/ticket")
                        .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(UUID.randomUUID()).lotteryId(lotteryResponse.id()).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isGone())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Out of tickets")))
                .andExpect(jsonPath("$.status", is("GONE")));
    }

    @Test
    public void testAllotMax_2TicketLottery_SameUser_Attempt_TwoTime_First_Get_Success_And_Second_UserAlreadyIssued_a_Ticket_Error()
            throws Exception {

        // Create a lottery with 2 ticket
        MvcResult mvcResult = mvc.perform(post("/api/v1/lottery")
                        .content(objectMapper.writeValueAsString(CreateLotteryRequest.builder().lotteryName("2_user_ticket_lottery_v2").totalLotteryTicketAllotted(2L).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("2_user_ticket_lottery_v2")))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        LotteryResponse lotteryResponse
                = objectMapper.readValue(responseBody, LotteryResponse.class);

        UUID userId = UUID.randomUUID();

        // Issue a ticket for the user - success
        mvc.perform(post("/api/v1/ticket")
                        .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(userId).lotteryId(lotteryResponse.id()).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lottery.name", is("2_user_ticket_lottery_v2")));

        // Issue a ticket for the same user, expecting an UserAlreadyIssuedTicket error
        mvc.perform(post("/api/v1/ticket")
                        .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(userId).lotteryId(lotteryResponse.id()).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User already issued a ticket")))
                .andExpect(jsonPath("$.status", is("FORBIDDEN")));
    }

    @Test
    public void allocate_30_tickets_Lottery_and_issue_tickets_return_ok() throws Exception {

        // Create a lottery with 30 tickets
        String lotteryId = "30_user_ticket_lottery";
        MvcResult mvcResult = mvc.perform(post("/api/v1/lottery")
                        .content(objectMapper.writeValueAsString(CreateLotteryRequest.builder().lotteryName(lotteryId).totalLotteryTicketAllotted(30L).build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        LotteryResponse lotteryResponse
                = objectMapper.readValue(responseBody, LotteryResponse.class);

        // Issue tickets for all 30 tickets
        for (int i = 1; i <= 30; i++) {
            mvc.perform(post("/api/v1/ticket")
                            .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(UUID.randomUUID()).lotteryId(lotteryResponse.id()).build()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated());

            if (i == 30) {
                mvc.perform(post("/api/v1/ticket")
                                .content(objectMapper.writeValueAsString(IssueTicketRequest.builder().userId(UUID.randomUUID()).lotteryId(lotteryResponse.id()).build()))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isGone())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message", is("Out of tickets")))
                        .andExpect(jsonPath("$.status", is("GONE")));
            }
        }

        // list all 30 user tickets
        mvc.perform(get("/api/v1/lottery/{uuid}/ticket/list", lotteryResponse.id()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(30)));
    }
}

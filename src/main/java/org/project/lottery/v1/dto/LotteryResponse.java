package org.project.lottery.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record LotteryResponse(
        @JsonProperty("name")
        String name,
        @JsonProperty("lottery_id")
        UUID id,
        @JsonProperty("total_allotted_tickets")
        Long totalAllottedTickets,
        @JsonProperty("total_available_tickets")
        Long totalAvailableTickets
) {
}

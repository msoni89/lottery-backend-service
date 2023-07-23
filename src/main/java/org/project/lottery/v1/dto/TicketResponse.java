package org.project.lottery.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.UUID;

@JsonSerialize
public record TicketResponse(@JsonProperty("ticket_id")
                             UUID id,
                             @JsonProperty("user_id")
                             UUID userId,

                             @JsonProperty("lottery_number")
                             String lotteryNumber,

                             @JsonProperty("lottery")
                             LotteryResponse lotteryResponse
) {
}

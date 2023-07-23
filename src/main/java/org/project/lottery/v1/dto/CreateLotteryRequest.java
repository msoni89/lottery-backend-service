package org.project.lottery.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateLotteryRequest(
        @JsonProperty("lottery_name")
        @NotNull(message = "lottery lotteryId must not be null") String lotteryName,
        @JsonProperty("available_lottery_ticket")
        @NotNull(message = "available lottery must not be null") long totalLotteryTicketAllotted
) {
}

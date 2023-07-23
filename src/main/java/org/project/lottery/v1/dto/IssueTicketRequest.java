package org.project.lottery.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@JsonSerialize
@Builder
public record IssueTicketRequest(
        @JsonProperty("user_id")
        @NotNull(message = "userId must not be null") UUID userId,
        @JsonProperty("lottery_id")
        @NotNull(message = "lotteryId must not be null") UUID lotteryId
) {
}

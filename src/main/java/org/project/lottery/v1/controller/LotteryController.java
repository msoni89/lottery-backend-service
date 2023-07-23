package org.project.lottery.v1.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.project.lottery.v1.dto.CreateLotteryRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Lottery;
import org.project.lottery.v1.service.lottery.ILotteryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lottery")
@AllArgsConstructor
@Validated
public class LotteryController {

    /**
     * The lottery service.
     */
    private final ILotteryService lotteryService;

    /**
     * Creates a lottery.
     *
     * @param request The create lottery request.
     * @return The lottery response.
     */
    @PostMapping
    public ResponseEntity<LotteryResponse> createLottery(
            @Valid @RequestBody CreateLotteryRequest request) {
        // Create the lottery.
        LotteryResponse lotteryResponse = lotteryService.createLottery(request);

        // Create the URI for the newly created lottery.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(lotteryResponse.id())
                .toUri();

        // Return the created lottery.
        return ResponseEntity.created(location).body(lotteryResponse);
    }

    /**
     * Gets a lottery by uuid.
     *
     * @param uuid The lotteryId of the lottery.
     * @return The lottery response, or {@code null} if no such lottery exists.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<LotteryResponse> findById(@NotNull @PathVariable("uuid") UUID uuid) {
        // Get the lottery by lotteryId.
        Lottery lottery = lotteryService.findById(uuid);

        // Return the lottery.
        return ResponseEntity.ok().body(new LotteryResponse(
                lottery.getName(),
                lottery.getId(),
                lottery.getTotalAllottedTickets(),
                lottery.getTotalAvailableTickets()
        ));
    }

    /**
     * Gets a list of all tickets for the specified lottery.
     *
     * @param uuid The lottery ID.
     * @return A list of all tickets for the specified lottery.
     */
    @GetMapping("/{uuid}/ticket/list")
    public ResponseEntity<List<TicketResponse>> listTickets(@NotNull @PathVariable("uuid") UUID uuid) {
        // Get the list of tickets for the specified lottery.
        List<TicketResponse> tickets = lotteryService.listTickets(uuid);

        // Return the list of tickets.
        return ResponseEntity.ok().body(tickets);
    }

}

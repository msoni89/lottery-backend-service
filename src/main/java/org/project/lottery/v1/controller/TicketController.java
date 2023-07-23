package org.project.lottery.v1.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.project.lottery.exceptions.NotFoundException;
import org.project.lottery.v1.dto.IssueTicketRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Ticket;
import org.project.lottery.v1.service.ticket.ITicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ticket")
@AllArgsConstructor
@Validated
public class TicketController {

    private final ITicketService ticketService;

    /**
     * Issue a new ticket to user.
     *
     * @param request The request body containing the user and lottery ID.
     * @return The newly issued ticket.
     */
    @PostMapping
    public ResponseEntity<TicketResponse> issueTicket(@Valid @RequestBody IssueTicketRequest request) {
        // Issue the ticket and get the response.
        TicketResponse ticketResponse = ticketService.issueTicket(request);

        // Create a URI pointing to the newly issued ticket.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(ticketResponse.id().toString())
                .toUri();

        // Return the ticket response with the location header set.
        return ResponseEntity.created(location).body(ticketResponse);
    }

    /**
     * Gets a ticket by UUID.
     *
     * @param uuid The UUID of the ticket.
     * @return The ticket, or {@code null} if no such ticket exists.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<TicketResponse> findById(@NotNull @PathVariable("id") UUID uuid) {
        // Get the ticket by UUID.
        Optional<Ticket> ticket = ticketService.findById(uuid);

        // If the ticket exists, create a TicketResponse from it.
        TicketResponse ticketResponse = ticket.map((_ticket ->
                new TicketResponse(
                        _ticket.getId(),
                        _ticket.getUserId(),
                        _ticket.getLotteryNumber(),
                        new LotteryResponse(
                                _ticket.getLottery().getName(),
                                _ticket.getLottery().getId(),
                                _ticket.getLottery().getTotalAllottedTickets(),
                                _ticket.getLottery().getTotalAvailableTickets()
                        )))
        ).orElseThrow(() -> new NotFoundException(String.format("Ticket not found for given id %s", uuid.toString())));

        // Return the ticket response.
        return ResponseEntity.ok().body(ticketResponse);
    }
}

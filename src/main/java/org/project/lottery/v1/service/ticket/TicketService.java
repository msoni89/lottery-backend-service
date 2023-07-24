package org.project.lottery.v1.service.ticket;

import lombok.extern.slf4j.Slf4j;
import org.project.lottery.exceptions.OutOfTicketsException;
import org.project.lottery.exceptions.UserAlreadyIssuedTicketException;
import org.project.lottery.locker.LockExecutionResult;
import org.project.lottery.locker.RedisDistributedLocker;
import org.project.lottery.v1.dto.IssueTicketRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Lottery;
import org.project.lottery.v1.entity.Ticket;
import org.project.lottery.v1.repository.ITicketRepository;
import org.project.lottery.v1.service.lottery.ILotteryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketService implements ITicketService {
    /**
     * The lock key for the lottery ticket resource.
     */
    private static final String LOTTERY_TICKET_LOCK_KEY = "lottery_%s_ticket_lock_key";

    /**
     * The ticket repository.
     */
    private final ITicketRepository ticketRepository;

    /**
     * The lottery service.
     */
    private final ILotteryService lotteryService;

    /**
     * The Redis distributed locker.
     */
    private final RedisDistributedLocker redisDistributedLocker;

    /**
     * Constructs a new TicketService instance.
     *
     * @param ticketRepository       The ticket repository.
     * @param lotteryService         The lottery service.
     * @param redisDistributedLocker The Redis distributed locker.
     */
    public TicketService(final ITicketRepository ticketRepository,
                         ILotteryService lotteryService,
                         final RedisDistributedLocker redisDistributedLocker
    ) {
        this.lotteryService = lotteryService;
        this.ticketRepository = ticketRepository;
        this.redisDistributedLocker = redisDistributedLocker;
    }

    /**
     * Issues a ticket for the specified lottery.
     *
     * @param request The issue ticket request.
     * @return The ticket response.
     */
    @Override
    public TicketResponse issueTicket(IssueTicketRequest request) {
        // Acquire a lock for the lottery ticket resource.
        LockExecutionResult<TicketResponse> result = redisDistributedLocker.lock(
                String.format(LOTTERY_TICKET_LOCK_KEY, request.lotteryId()),
                5,
                () -> {
                    // Get the lottery by ID.
                    Lottery lottery = lotteryService.findById(request.lotteryId());

                    // Get the current ticket count.
                    Long totalAvailableTickets = lottery.getTotalAvailableTickets();

                    // If the new count is greater than the maximum number of tickets, throw an exception.
                    if (totalAvailableTickets == 0) {
                        log.info("Ticket issuance failed: out of tickets");
                        throw new OutOfTicketsException("Out of tickets");
                    }

                    // If the user has already issued a ticket for given lottery, throw an exception.
                    if (ticketRepository.findByUserIdAndLotteryId(request.userId(), request.lotteryId()).isPresent()) {
                        log.info("Ticket issuance failed: user already issued a ticket");
                        throw new UserAlreadyIssuedTicketException("User already issued a ticket");
                    }

                    // Create a new ticket.
                    Ticket ticket = new Ticket(request.userId(), String.format("lottery_number_%d", totalAvailableTickets), lottery);

                    // Save the ticket to the database.
                    ticketRepository.save(ticket);
                    log.info("Ticket issued successfully: ticket = {}", ticket);
                    // Decrement the ticket total available tickets and save into database.
                    lottery.setTotalAvailableTickets(totalAvailableTickets - 1);

                    // Update the ticket count in the database.
                    lotteryService.save(lottery);

                    // Return the ticket response.
                    return new TicketResponse(ticket.getId(), ticket.getUserId(),
                            ticket.getLotteryNumber(),
                            new LotteryResponse(
                                    lottery.getName(),
                                    lottery.getId(),
                                    lottery.getTotalAllottedTickets(),
                                    lottery.getTotalAvailableTickets()
                            )
                    );
                }
        );

        // Log the result of the lock acquisition.
        log.info("Task result : '{}' -> exception : '{}'", result.getResultIfLockAcquired(), result.hasException());

        // If the lock acquisition failed, throw an exception.
        if (result.hasException()) {
            throw new RuntimeException(result.exception);
        }

        // Return the ticket response.
        return result.getResultIfLockAcquired();
    }

    /**
     * Get the ticket by ID from the repository.
     *
     * @param uuid The ticket ID.
     * @return The ticket.
     */
    @Cacheable(value = "tickets", key = "#uuid")
    public Optional<Ticket> findById(UUID uuid) {
        // Get the ticket by ID from the repository.
        log.info("Ticket by id '{}'", uuid);
        return ticketRepository.findById(uuid);
    }
}

package org.project.lottery.v1.service.lottery;

import lombok.extern.slf4j.Slf4j;
import org.project.lottery.exceptions.NotFoundException;
import org.project.lottery.v1.dto.CreateLotteryRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Lottery;
import org.project.lottery.v1.repository.ILotteryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LotteryService implements ILotteryService {

    // Create a private field to store the lottery repository.
    private final ILotteryRepository lotteryRepository;

    // Create a constructor to initialize the lottery repository.
    public LotteryService(ILotteryRepository lotteryRepository) {
        this.lotteryRepository = lotteryRepository;
    }

    // Create a method to create a lottery.
    @Override
    public LotteryResponse createLottery(CreateLotteryRequest request) {

        // Create a new lottery object.
        Lottery lottery = new Lottery();

        // Set the lottery lotteryId.
        lottery.setName(request.lotteryName());

        // Set the total allotted tickets.
        lottery.setTotalAllottedTickets(request.totalLotteryTicketAllotted());

        // Set the total available tickets.
        lottery.setTotalAvailableTickets(request.totalLotteryTicketAllotted());

        // Save the lottery in the repository.
        Lottery savedLottery = lotteryRepository.save(lottery);

        // Create a new LotteryResponse object.
        return new LotteryResponse(
                savedLottery.getName(),
                savedLottery.getId(),
                savedLottery.getTotalAllottedTickets(),
                savedLottery.getTotalAvailableTickets()
        );
    }

    // Create a method to find a lottery by ID.
    @Override
    @Cacheable(value = "lotteries", key = "#id")
    public Lottery findById(UUID id) {

        // Get the lottery from the repository by ID.
        Optional<Lottery> lotteryOptional = lotteryRepository.findById(id);
        // Throw an exception if the lottery does not exist.
        log.info("Lottery by id '{}'", id);
        // Create a new LotteryResponse object.
        return lotteryOptional.orElseThrow(() -> new NotFoundException(
                String.format("Lottery with lotteryId %s does not exist", id)
        ));
    }

    @Override
    @CachePut(value = "lotteries",key = "#lottery.id")
    @CacheEvict(value="lotteries", key = "#lottery.id")
    public void save(Lottery lottery) {
        lotteryRepository.save(lottery);
    }


    /**
     * Get a list of all tickets by lottery lotteryId.
     *
     * @param uuid The lottery lotteryId.
     * @return A list of tickets.
     */
    @Override
    public List<TicketResponse> listTickets(UUID uuid) {
        // Get a list of all ticket by lottery lotteryId.
        List<TicketResponse> ticketResponses = findById(uuid)
                .getTickets()
                .stream()
                .map(ticketResponse -> {
                    // Create a new TicketResponse object.
                    return new TicketResponse(
                            ticketResponse.getId(),
                            ticketResponse.getUserId(),
                            ticketResponse.getLotteryNumber(),
                            new LotteryResponse(
                                    ticketResponse.getLottery().getName(),
                                    ticketResponse.getLottery().getId(),
                                    ticketResponse.getLottery().getTotalAllottedTickets(),
                                    ticketResponse.getLottery().getTotalAvailableTickets()
                            )
                    );
                })
                .collect(Collectors.toList());

        // Log the list of ticket response.
        log.info("tickets: {}", ticketResponses);

        // Return the ticket.
        return ticketResponses;
    }

}

package org.project.lottery.v1.service.lottery;

import org.project.lottery.v1.dto.CreateLotteryRequest;
import org.project.lottery.v1.dto.LotteryResponse;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Lottery;

import java.util.List;
import java.util.UUID;

public interface ILotteryService {
    LotteryResponse createLottery(CreateLotteryRequest request);

    Lottery findById(UUID id);

    List<TicketResponse> listTickets(UUID uuid);

    void save(Lottery lottery);
}

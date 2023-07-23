package org.project.lottery.v1.service.ticket;

import org.project.lottery.v1.dto.IssueTicketRequest;
import org.project.lottery.v1.dto.TicketResponse;
import org.project.lottery.v1.entity.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ITicketService {
    TicketResponse issueTicket(IssueTicketRequest request);
    Optional<Ticket> findById(UUID uuid);


}

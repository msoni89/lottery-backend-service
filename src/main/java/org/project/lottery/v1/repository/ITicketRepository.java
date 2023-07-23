package org.project.lottery.v1.repository;

import org.project.lottery.v1.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ITicketRepository extends JpaRepository<Ticket, UUID> {
    @Query("select t from Ticket t where t.userId  = :userId and t.lottery.id = :lotteryId")
    Optional<Ticket> findByUserIdAndLotteryId(UUID userId, UUID lotteryId);

    @Query("select t from Ticket t where t.lottery.id = :lotteryId")
    Collection<Ticket> findAllByLotteryId(String lotteryId);
}

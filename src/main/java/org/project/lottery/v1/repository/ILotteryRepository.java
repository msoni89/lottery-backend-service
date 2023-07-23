package org.project.lottery.v1.repository;

import org.project.lottery.v1.entity.Lottery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ILotteryRepository extends JpaRepository<Lottery, UUID> {

}

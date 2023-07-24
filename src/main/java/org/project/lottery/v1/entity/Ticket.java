package org.project.lottery.v1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Data
public class Ticket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_uuid")
    private UUID userId;
    @Column(name = "lottery_number")
    private String lotteryNumber;

    @ManyToOne
    @JoinColumn(name = "lottery_id", nullable = false)
    private Lottery lottery;

    public Ticket(UUID userId, String lotteryNumber, Lottery lottery) {
        this.lotteryNumber = lotteryNumber;
        this.userId = userId;
        this.lottery = lottery;
    }
}

package org.project.lottery.v1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "lotteries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Lottery implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "total_allotted_tickets")
    private Long totalAllottedTickets;

    @Column(name = "total_available_tickets")
    private Long totalAvailableTickets;

    @OneToMany(mappedBy = "lottery")
    private Set<Ticket> tickets;

}

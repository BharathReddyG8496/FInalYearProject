package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Rating {
    @Id
    @GeneratedValue
    private int RatingId;
    private int ConsumerId;
    private int FarmerId;
    private int Score;
    private String Comment;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId")
    private Consumer consumer;
}

package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@AllArgsConstructor
@NoArgsConstructor
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

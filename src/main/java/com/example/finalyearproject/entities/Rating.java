package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class Rating {
    @Id
    @GeneratedValue
    private int RatingId;
    private int ConsumerId;
    private int FarmerId;
    private int Score;
    private String Comment;
    private LocalDateTime timestamp;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId")
    private Consumer consumer;
}

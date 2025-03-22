package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int RatingId;

    @NotNull(message = "Consumer ID cannot be null")
    private int ConsumerId;

    @NotNull(message = "Farmer ID cannot be null")
    private int FarmerId;

    @NotNull(message = "Score cannot be null")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot exceed 5")
    private int Score;

    @NotBlank(message = "Comment cannot be null")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String Comment;

    @PastOrPresent(message = "Timestamp must be in the past or present")
    private LocalDateTime timestamp;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId", insertable = false, updatable = false)
    private Consumer consumer;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FarmerId",insertable = false,updatable = false)
    private Farmer farmer;

}

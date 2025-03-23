package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    /*
    As these two attributes are FK, no need to specify it explicitly.
    The Framework will create FK on its own, at that case these below provided
    attributes might cause redundancy.

    This(your approach) has to be done in case when the Framework is following lot more
    Bare metal approach.
     */
//    @NotNull(message = "Consumer ID cannot be null")
//    private int ConsumerId;
//
//    @NotNull(message = "Farmer ID cannot be null")
//    private int FarmerId;

    @NotNull(message = "Score cannot be null")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot exceed 5")
    private int Score;

    @NotBlank(message = "Comment cannot be null")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String Comment;

    @PastOrPresent(message = "Timestamp must be in the past or present")
    private LocalDateTime timestamp;

    @ManyToOne()
    @JsonBackReference
//    @JoinColumn(name = "ConsumerId", insertable = false, updatable = false)
    private Consumer consumer;

    @ManyToOne()
    @JsonBackReference
//    @JoinColumn(name = "FarmerId",insertable = false,updatable = false)
    private Farmer farmer;

}

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
    private int ratingId;

    @NotNull(message = "Score cannot be null")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot exceed 5")
    private int Score;

    @NotBlank(message = "Comment cannot be null")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String Comment;

    private LocalDateTime timestamp;

    @ManyToOne
//    @JoinColumn(name = "ConsumerId",insertable = false,updatable = false)
    @JsonBackReference("consumer-ratings")
    private Consumer consumer;

    @ManyToOne
    @JsonBackReference("product-ratings")
//    @JoinColumn(name = "FarmerId",insertable = false,updatable = false)
    private Product product;

    @Override
    public String toString() {
        return "Rating{" +
                "ratingId=" + ratingId +
                ", Score=" + Score +
                ", Comment='" + Comment + '\'' +
                ", timestamp=" + timestamp +
                ", consumer=" + consumer +
                ", product=" + product +
                '}';
    }
}

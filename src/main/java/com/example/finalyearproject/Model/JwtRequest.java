package com.example.finalyearproject.Model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class JwtRequest {
    private String consumerName;
    private String consumerPassword;
}
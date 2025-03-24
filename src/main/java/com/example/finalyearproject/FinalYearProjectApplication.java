package com.example.finalyearproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.finalyearproject.Abstraction",
        "com.example.finalyearproject.Security"
})
@EnableJpaRepositories(basePackages = "com.example.finalyearproject.Abstraction")  // Ensure correct package
@EntityScan(basePackages = "com.example.finalyearproject.DataStore")
public class FinalYearProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalYearProjectApplication.class, args);
    }

}

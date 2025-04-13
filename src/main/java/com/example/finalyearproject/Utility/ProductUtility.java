package com.example.finalyearproject.Utility;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ProductUtility {
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    // Exclude images from being mapped to your domain entity directly
    private MultipartFile[] images;
}

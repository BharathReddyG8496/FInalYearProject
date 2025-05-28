package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private int id;
    private String filename;
    private String filePath;
}

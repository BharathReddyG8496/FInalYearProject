package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private String value;  // The enum constant name (e.g., "FRUITS")
    private String displayName;  // The display name (e.g., "Fruits")
}
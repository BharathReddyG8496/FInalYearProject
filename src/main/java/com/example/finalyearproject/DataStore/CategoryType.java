package com.example.finalyearproject.DataStore;

import lombok.Getter;

@Getter
public enum CategoryType {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    DAIRY("Dairy Products"),
    GRAINS("Grains & Cereals"),
    MEAT("Meat & Poultry"),
    BEVERAGES("Beverages");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }

}
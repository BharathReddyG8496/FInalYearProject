package com.example.finalyearproject.DataStore;

import lombok.Getter;

@Getter
public enum Unit {
    // Weight units
    KILOGRAM("kg"),
    GRAM("g"),
    POUND("lb"),
    TON("ton"),
    QUINTAL("quintal"),

    // Volume units
    LITRE("L"),
    MILLILITRE("mL"),
    GALLON("gal"),

    // Count units
    PIECE("piece"),
    DOZEN("dozen"),
    BUNDLE("bundle"),
    BOX("box"),
    PACKET("packet"),
    BAG("bag"),

    // Length units (for some agricultural products like sugarcane)
    METER("m"),
    FOOT("ft");

    private final String displayName;

    Unit(String displayName) {
        this.displayName = displayName;
    }

}

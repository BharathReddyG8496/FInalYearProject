package com.example.finalyearproject.Utility;


import com.example.finalyearproject.DataStore.Farmer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FarmerUtility {
    private int statusCode;
    private String message;
    private Farmer farmer ;
}

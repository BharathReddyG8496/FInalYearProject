package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Services.DonationService;
import com.example.finalyearproject.Utility.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    // Get farmers eligible for donation
    @GetMapping("/eligible-farmers")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<EligibleFarmerDTO>>> getEligibleFarmers(
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<List<EligibleFarmerDTO>> response =
                donationService.getEligibleFarmersForDonation(consumerEmail);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/donate")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<DonationResponseDTO>> donate(
            @RequestBody @Valid DonationRequestDTO donationRequest,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<DonationResponseDTO> response = donationService.createDonation(donationRequest, consumerEmail);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/consumer/history")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<DonationResponseDTO>>> getConsumerDonations(
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<List<DonationResponseDTO>> response = donationService.getConsumerDonations(consumerEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/farmer/received")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<DonationResponseDTO>>> getFarmerDonations(
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<List<DonationResponseDTO>> response = donationService.getFarmerDonations(farmerEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('CONSUMER', 'FARMER')")
    public ResponseEntity<ApiResponse<DonationStatsDTO>> getDonationStats(
            Authentication authentication) {

        String userEmail = authentication.getName();
        String userType = authentication.getAuthorities().iterator().next().getAuthority();

        ApiResponse<DonationStatsDTO> response = donationService.getDonationStats(userEmail, userType);

        return ResponseEntity.ok(response);
    }

    // Webhook endpoint for payment gateway callbacks (no auth required)
    @PostMapping("/payment/callback")
    public ResponseEntity<ApiResponse<Void>> paymentCallback(
            @RequestParam String transactionId,
            @RequestParam boolean success,
            @RequestParam(required = false) String failureReason) {

        ApiResponse<Void> response = donationService.updatePaymentStatus(transactionId, success, failureReason);

        return ResponseEntity.ok(response);
    }
}
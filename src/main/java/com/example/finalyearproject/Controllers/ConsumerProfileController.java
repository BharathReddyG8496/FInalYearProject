package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.ConsumerUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/consumer/profile")
public class ConsumerProfileController {

    @Autowired
    private ConsumerService consumerService;

    /**
     * Get consumer profile
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Consumer>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        ApiResponse<Consumer> response = consumerService.getConsumerByEmail(email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Update consumer profile
     */
    @PutMapping
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Consumer>> updateProfile(
            @Valid @RequestBody ConsumerUpdateDTO updateDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Consumer currentConsumer = consumerService.findByEmail(email);

        if (currentConsumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Update failed", "Consumer not found"));
        }

        ApiResponse<Consumer> response = consumerService.updateConsumer(updateDTO, email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Update profile photo
     */
    @PostMapping("/photo")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<String>> updateProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String email = authentication.getName();
        ApiResponse<String> response = consumerService.updateProfilePhoto(file, email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Get all delivery addresses
     */
    @GetMapping("/addresses")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Set<DeliveryAddresses>>> getAddresses(
            Authentication authentication) {

        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Get addresses failed", "Consumer not found"));
        }

        ApiResponse<Set<DeliveryAddresses>> response =
                consumerService.getDeliveryAddresses(consumer.getConsumerId());

        return ResponseEntity.ok(response);
    }

    /**
     * Add a new delivery address
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Set<DeliveryAddresses>>> addAddress(
            @Valid @RequestBody DeliveryAddresses address,
            Authentication authentication) {

        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Add address failed", "Consumer not found"));
        }

        ApiResponse<Set<DeliveryAddresses>> response =
                consumerService.AddDeliveryAddress(address, consumer.getConsumerId());

        return response.isSuccess()
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Update an existing delivery address
     */
    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<DeliveryAddresses>> updateAddress(
            @Valid @RequestBody DeliveryAddresses address,
            @PathVariable int addressId,
            Authentication authentication) {

        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Update address failed", "Consumer not found"));
        }

        ApiResponse<DeliveryAddresses> response =
                consumerService.UpdateDeliveryAddress(address, consumer.getConsumerId(), addressId);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Delete a delivery address
     */
    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Set<DeliveryAddresses>>> deleteAddress(
            @PathVariable int addressId,
            Authentication authentication) {

        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Delete address failed", "Consumer not found"));
        }

        ApiResponse<Set<DeliveryAddresses>> response =
                consumerService.DeleteDeliveryAddress(addressId, consumer.getConsumerId());

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
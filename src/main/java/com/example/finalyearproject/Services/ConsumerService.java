package com.example.finalyearproject.Services;

import com.cloudinary.Cloudinary;
import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.DeliveryAddressesRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.ConsumerRegisterDTO;
import com.example.finalyearproject.Utility.ConsumerUpdateDTO;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private DeliveryAddressesRepo deliveryAddressesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    private static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    /**
     * Find a consumer by email
     */
    public Consumer findByEmail(String email) {
        try {
            return consumerRepo.findByConsumerEmail(email);
        } catch (Exception e) {
            logger.error("Error finding consumer by email: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get consumer profile by email
     */
    public ApiResponse<Consumer> getConsumerByEmail(String email) {
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(email);
            if (consumer == null) {
                return ApiResponse.error("Consumer not found", "No consumer found with email: " + email);
            }
            return ApiResponse.success("Consumer profile retrieved successfully", consumer);
        } catch (Exception e) {
            logger.error("Error retrieving consumer profile: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve consumer profile", e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Consumer> RegisterConsumer(ConsumerRegisterDTO dto) {
        try {
            // Check for existing email first
            if (consumerRepo.findByConsumerEmail(dto.getConsumerEmail()) != null) {
                return ApiResponse.error("Registration failed", "Email already registered");
            }

            // Check for existing phone if you have a repository method for it
            if (consumerRepo.findByConsumerPhone(dto.getConsumerPhone()) != null) {
                return ApiResponse.error("Registration failed", "Phone number already registered");
            }

            Consumer consumer = new Consumer();
            consumer.setConsumerFirstName(dto.getConsumerFirstName());
            consumer.setConsumerLastName(dto.getConsumerLastName());
            consumer.setConsumerEmail(dto.getConsumerEmail());
            consumer.setConsumerPhone(dto.getConsumerPhone());
            consumer.setConsumerAddress(dto.getConsumerAddress());
            consumer.setConsumerPassword(passwordEncoder.encode(dto.getConsumerPassword()));

            // Handle profile image upload
            if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
                try {
                    String imagePath = uploadConsumerProfilePhoto(dto.getProfilePhoto(), dto.getConsumerEmail());
                    consumer.setProfilePhotoPath(imagePath);
                } catch (IOException e) {
                    logger.error("Failed to upload profile photo: {}", e.getMessage());
                    // Continue with registration without the profile photo
                }
            }

            Consumer saved = consumerRepo.save(consumer);
            return ApiResponse.success("Consumer registered successfully", saved);
        } catch (DataIntegrityViolationException e) {
            // Re-throw constraint violations to be handled by the global exception handler
            logger.error("Database constraint violation during consumer registration", e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to register consumer: {}", e.getMessage(), e);
            return ApiResponse.error("Registration failed", "An unexpected error occurred. Please try again.");
        }
    }

    /**
     * Original two-parameter method for backward compatibility
     */
    public String uploadConsumerProfilePhoto(MultipartFile file, String email) throws IOException {
        // Call the new three-parameter version with null for existingPublicId
        return uploadConsumerProfilePhoto(file, email, null);
    }

    /**
     * Upload consumer profile photo to Cloudinary
     * @param file The image file to upload
     * @param email Consumer's email (used for folder structure)
     * @param existingPublicId Optional existing public_id to overwrite
     * @return The URL of the uploaded image
     */
    public String uploadConsumerProfilePhoto(MultipartFile file, String email, String existingPublicId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile photo is empty or missing");
        }

        // File type validation
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Uploaded file is not an image");
        }

        // Size validation (e.g., 5MB max)
        long maxSizeBytes = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Image size exceeds maximum allowed (5MB)");
        }

        try {
            // Create a folder path for organizing images in Cloudinary
            String folderPath = "profiles/consumers/" + email.replaceAll("[^a-zA-Z0-9.\\-]", "_");

            // Prepare upload parameters
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folderPath);

            // Use existing public_id if available, otherwise generate new one
            if (existingPublicId != null && !existingPublicId.isEmpty()) {
                // If we have a full path including folder, extract just the filename part
                if (existingPublicId.contains("/")) {
                    existingPublicId = existingPublicId.substring(existingPublicId.lastIndexOf('/') + 1);
                }
                params.put("public_id", existingPublicId);
                logger.info("Overwriting existing image with public_id: {}", existingPublicId);
            } else {
                params.put("public_id", UUID.randomUUID().toString());
                logger.info("Creating new image with generated public_id");
            }

            params.put("overwrite", true);
            params.put("resource_type", "auto");

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // Validate return value
            if (uploadResult == null || !uploadResult.containsKey("secure_url")) {
                throw new IOException("Invalid response from image upload service");
            }

            // Return the secure URL of the uploaded image
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            logger.error("Failed to upload image to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.error("Cloudinary upload error: {}", e.getMessage(), e);
            throw new IOException("Image upload service failed: " + e.getMessage(), e);
        }
    }

    /**
     * Update consumer profile photo
     */
    @Transactional
    public ApiResponse<String> updateProfilePhoto(MultipartFile file, String email) {
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(email);
            if (consumer == null) {
                return ApiResponse.error("Update failed", "Consumer not found with email: " + email);
            }

            // Validate file
            if (file == null || file.isEmpty()) {
                return ApiResponse.error("Update failed", "Profile photo is empty or missing");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("Update failed", "Uploaded file is not an image");
            }

            long maxSizeBytes = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSizeBytes) {
                return ApiResponse.error("Update failed", "Image size exceeds maximum allowed (5MB)");
            }

            // Extract existing public_id if possible
            String publicId = null;
            String existingPhotoPath = consumer.getProfilePhotoPath();
            if (existingPhotoPath != null && existingPhotoPath.contains("cloudinary.com")) {
                try {
                    // Extract public_id from URL
                    // Format: https://res.cloudinary.com/cloud-name/image/upload/v1234567890/folder/public_id.ext
                    String[] parts = existingPhotoPath.split("/upload/");
                    if (parts.length > 1) {
                        String afterUpload = parts[1];
                        // Remove version number if present (v1234567890/)
                        if (afterUpload.startsWith("v")) {
                            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
                        }
                        // Remove file extension
                        publicId = afterUpload.substring(0, afterUpload.lastIndexOf('.'));
                    }
                } catch (Exception e) {
                    logger.warn("Could not extract public_id from existing photo URL: {}", existingPhotoPath);
                    // If extraction fails, we'll generate a new ID
                }
            }

            // Upload new photo (will overwrite if publicId was successfully extracted)
            String newPhotoUrl = uploadConsumerProfilePhoto(file, email, publicId);

            // Update consumer record
            consumer.setProfilePhotoPath(newPhotoUrl);
            consumerRepo.save(consumer);

            return ApiResponse.success("Profile photo updated successfully", newPhotoUrl);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file: {}", e.getMessage());
            return ApiResponse.error("Update failed", e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to upload profile photo: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating profile photo: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Consumer> updateConsumer(ConsumerUpdateDTO updateDTO, String email) {
        try {
            Consumer existingConsumer = consumerRepo.findByConsumerEmail(email);
            if (existingConsumer == null) {
                return ApiResponse.error("Update failed", "Consumer not found with email: " + email);
            }

            // Update only the fields provided in the DTO
            if (updateDTO.getConsumerFirstName() != null && !updateDTO.getConsumerFirstName().isEmpty()) {
                existingConsumer.setConsumerFirstName(updateDTO.getConsumerFirstName());
            }

            if (updateDTO.getConsumerLastName() != null && !updateDTO.getConsumerLastName().isEmpty()) {
                existingConsumer.setConsumerLastName(updateDTO.getConsumerLastName());
            }

            if (updateDTO.getConsumerPhone() != null && !updateDTO.getConsumerPhone().isEmpty()) {
                existingConsumer.setConsumerPhone(updateDTO.getConsumerPhone());
            }

            if (updateDTO.getConsumerAddress() != null && !updateDTO.getConsumerAddress().isEmpty()) {
                existingConsumer.setConsumerAddress(updateDTO.getConsumerAddress());
            }

//            // Only update password if provided and not empty
//            if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
//                existingConsumer.setConsumerPassword(passwordEncoder.encode(updateDTO.getPassword()));
//            }

            Consumer updatedConsumer = consumerRepo.save(existingConsumer);
            return ApiResponse.success("Consumer updated successfully", updatedConsumer);
        } catch (Exception e) {
            logger.error("Failed to update consumer: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Set<DeliveryAddresses>> AddDeliveryAddress(DeliveryAddresses deliveryAddress, int consumerId) {
        try {
            if (deliveryAddress == null) {
                return ApiResponse.error("Invalid request", "Delivery address cannot be null");
            }

            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            if (consumer == null) {
                return ApiResponse.error("Consumer not found", "No consumer found with ID: " + consumerId);
            }

            deliveryAddress.setConsumer(consumer);
            if (consumer.getSetOfDeliveryAddress() == null) {
                consumer.setSetOfDeliveryAddress(new HashSet<>());
            }

            consumer.getSetOfDeliveryAddress().add(deliveryAddress);
            consumerRepo.save(consumer);

            return ApiResponse.success("Delivery address added successfully", consumer.getSetOfDeliveryAddress());
        } catch (Exception e) {
            logger.error("Failed to add delivery address: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to add delivery address", e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<DeliveryAddresses> UpdateDeliveryAddress(DeliveryAddresses address, int consumerId, int addressId) {
        try {
            if (address == null) {
                return ApiResponse.error("Invalid request", "Delivery address cannot be null");
            }

            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            if (consumer == null) {
                return ApiResponse.error("Consumer not found", "No consumer found with ID: " + consumerId);
            }

            // Verify the address belongs to this consumer
            boolean addressBelongsToConsumer = consumer.getSetOfDeliveryAddress().stream()
                    .anyMatch(addr -> addr.getDeliveryAddressId() == addressId);

            if (!addressBelongsToConsumer) {
                return ApiResponse.error("Unauthorized", "Address does not belong to this consumer");
            }

            consumerRepo.updateDeliveryAddress(address, addressId, consumerId);
            DeliveryAddresses updatedAddress = deliveryAddressesRepo.findDeliveryAddressesByDeliveryAddressId(addressId);

            return ApiResponse.success("Delivery address updated successfully", updatedAddress);
        } catch (Exception e) {
            logger.error("Failed to update delivery address: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to update delivery address", e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Set<DeliveryAddresses>> DeleteDeliveryAddress(int addressId, int consumerId) {
        try {
            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            if (consumer == null) {
                return ApiResponse.error("Consumer not found", "No consumer found with ID: " + consumerId);
            }

            // Verify the address belongs to this consumer
            boolean addressBelongsToConsumer = consumer.getSetOfDeliveryAddress().stream()
                    .anyMatch(addr -> addr.getDeliveryAddressId() == addressId);

            if (!addressBelongsToConsumer) {
                return ApiResponse.error("Unauthorized", "Address does not belong to this consumer");
            }

            consumerRepo.deleteDeliveryAddressById(addressId, consumerId);
            Set<DeliveryAddresses> remainingAddresses = consumer.getSetOfDeliveryAddress();

            return ApiResponse.success("Delivery address deleted successfully", remainingAddresses);
        } catch (Exception e) {
            logger.error("Failed to delete delivery address: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to delete delivery address", e.getMessage());
        }
    }

    /**
     * Get all delivery addresses for a consumer
     */
    public ApiResponse<Set<DeliveryAddresses>> getDeliveryAddresses(int consumerId) {
        try {
            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            if (consumer == null) {
                return ApiResponse.error("Consumer not found", "No consumer found with ID: " + consumerId);
            }

            Set<DeliveryAddresses> addresses = consumer.getSetOfDeliveryAddress();
            if (addresses == null) {
                addresses = new HashSet<>();
            }

            return ApiResponse.success("Delivery addresses retrieved successfully", addresses);
        } catch (Exception e) {
            logger.error("Failed to get delivery addresses: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve delivery addresses", e.getMessage());
        }
    }
}
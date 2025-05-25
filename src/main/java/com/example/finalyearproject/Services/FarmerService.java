package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.FarmerRegisterDTO;
import com.cloudinary.Cloudinary;
import com.example.finalyearproject.Utility.FarmerUpdateDTO;
import com.example.finalyearproject.Utility.FarmerUtility;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FarmerService {

    private static final Logger logger = LoggerFactory.getLogger(FarmerService.class);

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ProductRepo productRepo;



    /**
     * Get farmer profile by email
     */
    public ApiResponse<Farmer> getFarmerByEmail(String email) {
        try {
            Farmer farmer = farmerRepo.findByFarmerEmail(email);
            if (farmer == null) {
                return ApiResponse.error("Farmer not found", "No farmer found with email: " + email);
            }
            return ApiResponse.success("Farmer profile retrieved successfully", farmer);
        } catch (Exception e) {
            logger.error("Error retrieving farmer profile: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve farmer profile", e.getMessage());
        }
    }

    /**
     * Get farmer statistics including sales, product count, ratings, etc.
     */
    public ApiResponse<Map<String, Object>> getFarmerStats(String email) {
        try {
            Farmer farmer = farmerRepo.findByFarmerEmail(email);
            if (farmer == null) {
                return ApiResponse.error("Stats retrieval failed", "Farmer not found");
            }

            // Create response data
            Map<String, Object> stats = new HashMap<>();

            // Basic farmer info
            stats.put("farmerId", farmer.getFarmerId());
            stats.put("name", farmer.getFarmerName());

            // Count products
            long productCount = productRepo.countByFarmer_FarmerId(farmer.getFarmerId());
            stats.put("productCount", productCount);

            // Get ratings info
            stats.put("averageRating", farmer.getAverageRating());
            stats.put("ratingCount", farmer.getRatingCount());

            // Get sales statistics
            // For now, let's make a basic implementation without modifying repositories
            // You can enhance this with actual database queries later

            // For example, to count products with low stock
            long lowStockProducts = productRepo.countByFarmer_FarmerIdAndStockLessThan(
                    farmer.getFarmerId(), 10); // Products with less than 10 items
            stats.put("lowStockProducts", lowStockProducts);

            return ApiResponse.success("Farmer statistics retrieved successfully", stats);
        } catch (Exception e) {
            logger.error("Failed to retrieve farmer stats: {}", e.getMessage(), e);
            return ApiResponse.error("Stats retrieval failed", e.getMessage());
        }
    }


    @Transactional
    public ApiResponse<Farmer> RegisterFarmer(FarmerRegisterDTO dto) {
        try {
            // Check for existing email
            if (farmerRepo.findByFarmerEmail(dto.getFarmerEmail()) != null) {
                return ApiResponse.error("Registration failed", "Email already registered");
            }

            // Check for existing phone if you have a repository method for it
            if (farmerRepo.findByFarmerPhone(dto.getFarmerPhone()) != null) {
                return ApiResponse.error("Registration failed", "Phone number already registered");
            }

            Farmer farmer = new Farmer();
            farmer.setFarmerEmail(dto.getFarmerEmail());
            farmer.setFirstName(dto.getFirstName());
            farmer.setLastName(dto.getLastName());
            farmer.setFarmerPassword(passwordEncoder.encode(dto.getFarmerPassword()));
            farmer.setFarmerPhone(dto.getFarmerPhone());
            farmer.setFarmerAddress(dto.getFarmerAddress());

            // Upload profile photo if provided
            if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
                try {
                    String imageUrl = uploadProfilePhoto(dto.getProfilePhoto(), dto.getFarmerEmail());
                    farmer.setProfilePhotoPath(imageUrl);
                } catch (IOException e) {
                    logger.error("Failed to upload profile photo: {}", e.getMessage());
                    // Continue registration process without profile photo
                }
            }

            Farmer saved = farmerRepo.save(farmer);
            return ApiResponse.success("Farmer registered successfully", saved);
        } catch (DataIntegrityViolationException e) {
            // Re-throw constraint violations to be handled by the global exception handler
            logger.error("Database constraint violation during farmer registration", e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to register farmer: {}", e.getMessage(), e);
            return ApiResponse.error("Registration failed", "An unexpected error occurred. Please try again.");
        }
    }

    @Transactional
    public ApiResponse<FarmerUtility> updateFarmer(FarmerUpdateDTO updateDTO, String farmerEmail) {
        try {
            Farmer existingFarmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (existingFarmer == null) {
                return ApiResponse.error("Update failed", "Farmer not found with email: " + farmerEmail);
            }

            // Update only the fields provided in the DTO
            if (updateDTO.getFirstName() != null && !updateDTO.getFirstName().isEmpty()) {
                existingFarmer.setFirstName(updateDTO.getFirstName());
            }

            if (updateDTO.getLastName() != null && !updateDTO.getLastName().isEmpty()) {
                existingFarmer.setLastName(updateDTO.getLastName());
            }

            if (updateDTO.getFarmerPhone() != null && !updateDTO.getFarmerPhone().isEmpty()) {
                existingFarmer.setFarmerPhone(updateDTO.getFarmerPhone());
            }

            if (updateDTO.getFarmerAddress() != null && !updateDTO.getFarmerAddress().isEmpty()) {
                existingFarmer.setFarmerAddress(updateDTO.getFarmerAddress());
            }
            // Only update password if provided
//            if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
//                existingFarmer.setFarmerPassword(passwordEncoder.encode(updateDTO.getPassword()));
//            }

            // Save the updated farmer
            Farmer updatedAndSavedFarmer = farmerRepo.save(existingFarmer);
            FarmerUtility updatedFarmer=new FarmerUtility(
                    updatedAndSavedFarmer.getFirstName(),
                    updatedAndSavedFarmer.getLastName(),
                    updatedAndSavedFarmer.getFarmerPhone(),
                    updatedAndSavedFarmer.getFarmerAddress());
            return ApiResponse.success("Farmer updated successfully", updatedFarmer);
        } catch (Exception e) {
            logger.error("Failed to update farmer: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }
    /**
     * Upload profile photo to Cloudinary (simplified version that calls the full method)
     * @param file The image file to upload
     * @param email Farmer's email (used for folder structure)
     * @return The URL of the uploaded image
     */
    public String uploadProfilePhoto(MultipartFile file, String email) throws IOException {
        // Call the three-parameter version with null for existingPublicId
        return uploadProfilePhoto(file, email, null);
    }

    /**
     * Update farmer's profile photo
     * Overwrites existing photo if present
     */
    @Transactional
    public ApiResponse<String> updateProfilePhoto(MultipartFile file, String farmerEmail) {
        try {
            // Find the farmer
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Update failed", "Farmer not found with email: " + farmerEmail);
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
            String existingPhotoPath = farmer.getProfilePhotoPath();
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
                    publicId = null;
                }
            }

            // Upload new photo
            String newPhotoUrl = uploadProfilePhoto(file, farmerEmail, publicId);

            // Update farmer record
            farmer.setProfilePhotoPath(newPhotoUrl);
            farmerRepo.save(farmer);

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

    /**
     * Upload profile photo to Cloudinary
     * @param file The image file to upload
     * @param email Farmer's email (used for folder structure)
     * @param existingPublicId Optional existing public_id to overwrite
     * @return The URL of the uploaded image
     */
    public String uploadProfilePhoto(MultipartFile file, String email, String existingPublicId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile photo is empty or missing");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            // Create a folder path for organizing images in Cloudinary
            String folderPath = "profiles/farmers/" + email.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

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
            logger.error("Cloudinary service error: {}", e.getMessage(), e);
            throw new IOException("Cloud storage service error: " + e.getMessage(), e);
        }
    }
}
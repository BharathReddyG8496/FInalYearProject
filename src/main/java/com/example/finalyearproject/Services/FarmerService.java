package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.Utility.FarmerRegisterDTO;
import com.example.finalyearproject.Utility.FarmerUtility;
import com.cloudinary.Cloudinary;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    @Transactional
    public FarmerUtility RegisterFarmer(FarmerRegisterDTO dto) {
        try {
            // Check for existing email or phone
            if (farmerRepo.findByFarmerEmail(dto.getFarmerEmail()) != null) {
                return new FarmerUtility(400, "Email already registered", null);
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
            return new FarmerUtility(200, "Registered", saved);

        } catch (Exception e) {
            logger.error("Failed to register farmer: {}", e.getMessage(), e);
            return new FarmerUtility(500, "Failed to register: " + e.getMessage(), null);
        }
    }

    public String uploadProfilePhoto(MultipartFile file, String email) throws IOException {
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
            params.put("public_id", UUID.randomUUID().toString());
            params.put("overwrite", true);
            params.put("resource_type", "auto");

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

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

    @Transactional
    public Optional<Farmer> UpdateFarmer(Farmer farmer, String farmerEmail) {
        Farmer byFarmerEmail = farmerRepo.findByFarmerEmail(farmerEmail);
        try {
            farmerRepo.updateByFarmerId(farmer, byFarmerEmail.getFarmerId());
            return this.farmerRepo.findByFarmerId(byFarmerEmail.getFarmerId());
        } catch (Exception e) {
            logger.error("Failed to update farmer: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Update farmer's profile photo
     */
    @Transactional
    public String updateProfilePhoto(MultipartFile file, String farmerEmail) throws IOException {
        Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
        if (farmer == null) {
            throw new IllegalArgumentException("Farmer not found with email: " + farmerEmail);
        }

        // If farmer has an existing photo and it's a Cloudinary URL, try to delete it
        String existingPhotoPath = farmer.getProfilePhotoPath();
        if (existingPhotoPath != null && existingPhotoPath.contains("cloudinary.com")) {
            try {
                // Extract public_id from URL - this may need adjustment based on your URL format
                String publicId = existingPhotoPath.substring(
                        existingPhotoPath.lastIndexOf("/") + 1,
                        existingPhotoPath.lastIndexOf(".")
                );
                // Delete from Cloudinary
                Map<String, String> params = new HashMap<>();
                params.put("resource_type", "image");
                cloudinary.uploader().destroy(publicId, params);
            } catch (Exception e) {
                logger.warn("Failed to delete existing profile photo: {}", e.getMessage());
                // Continue with the upload even if deletion fails
            }
        }

        // Upload new photo
        String newPhotoUrl = uploadProfilePhoto(file, farmerEmail);

        // Update farmer record
        farmer.setProfilePhotoPath(newPhotoUrl);
        farmerRepo.save(farmer);

        return newPhotoUrl;
    }
}
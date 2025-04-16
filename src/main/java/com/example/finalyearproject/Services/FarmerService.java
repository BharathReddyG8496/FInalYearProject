package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.Utility.FarmerRegisterDTO;
import com.example.finalyearproject.Utility.FarmerUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class FarmerService {

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

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
                String imagePath = uploadProfilePhoto(dto.getProfilePhoto(), dto.getFarmerEmail());
                farmer.setProfilePhotoPath(imagePath);
            }

            Farmer saved = farmerRepo.save(farmer);
            return new FarmerUtility(200, "Registered", saved);

        } catch (Exception e) {
            return new FarmerUtility(500, "Failed to register: " + e.getMessage(), null);
        }
    }



    public String uploadProfilePhoto(MultipartFile file, String email) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile photo is empty or missing");
        }

        String safeEmail = email.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        Path profileDir = Paths.get(uploadDir, "profiles", safeEmail);
        if (!Files.exists(profileDir)) {
            Files.createDirectories(profileDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = profileDir.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath);

        return "/uploads/profiles/" + safeEmail + "/" + uniqueFilename;
    }


    public Optional<Farmer> UpdateFarmer(Farmer farmer, String farmerEmail){
        Farmer byFarmerEmail = farmerRepo.findByFarmerEmail(farmerEmail);
       try{
           farmerRepo.updateByFarmerId(farmer,byFarmerEmail.getFarmerId());
           return this.farmerRepo.findByFarmerId(byFarmerEmail.getFarmerId());
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }
}

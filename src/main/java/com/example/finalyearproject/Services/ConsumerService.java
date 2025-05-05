package com.example.finalyearproject.Services;

import com.cloudinary.Cloudinary;
import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.DeliveryAddressesRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Utility.ConsumerRegisterDTO;
import com.example.finalyearproject.Utility.ConsumerUtility;
import com.example.finalyearproject.Utility.DeliveryAddressUtility;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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



    @Transactional
    public ConsumerUtility RegisterConsumer(ConsumerRegisterDTO dto) {
        try {
            if (consumerRepo.findByConsumerEmail(dto.getConsumerEmail()) != null) {
                return new ConsumerUtility(400, "Email already registered", null);
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
                String imagePath = uploadConsumerProfilePhoto(dto.getProfilePhoto(), dto.getConsumerEmail());
                consumer.setProfilePhotoPath(imagePath);  // Assuming you have this field in your Consumer entity
            }

            Consumer saved = consumerRepo.save(consumer);
            return new ConsumerUtility(200, "Registered", saved);

        } catch (Exception e) {
            return new ConsumerUtility(500, "Failed to register: " + e.getMessage(), null);
        }
    }

    public String uploadConsumerProfilePhoto(MultipartFile file, String email) throws IOException {
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
            params.put("public_id", UUID.randomUUID().toString());
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



    public void UpdateConsumer(Consumer consumer, int id){
        try {
            consumerRepo.updateConsumerByconsumerId(consumer, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Set<DeliveryAddresses> AddDeliveryAddress(DeliveryAddresses deliveryAddresses,int consumerId){
        if(deliveryAddresses!=null && consumerId!=0){
            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            deliveryAddresses.setConsumer(consumer);
            if (consumer.getSetOfDeliveryAddress() == null) {
                consumer.setSetOfDeliveryAddress(new HashSet<>());
            }
            consumer.getSetOfDeliveryAddress().add(deliveryAddresses);
            consumerRepo.save(consumer);
            return consumer.getSetOfDeliveryAddress();
        }
        return Collections.emptySet();
    }

    public DeliveryAddressUtility UpdateDeliveryAddress(DeliveryAddresses addresses,int consumerId,int addressId){
        if(addresses!=null && consumerId!=0 && addressId!=0){
            consumerRepo.updateDeliveryAddress(addresses,addressId,consumerId);
            DeliveryAddresses deliveryAddress = deliveryAddressesRepo.findDeliveryAddressesByDeliveryAddressId(addressId);
            return new DeliveryAddressUtility(200,"Updated",deliveryAddress);
        }
        return new DeliveryAddressUtility(400,"Failed to update",null);
    }

    public Set<DeliveryAddresses> DeleteDeliveryAddress(int addressId,int consumerId){
        if(addressId!=0 && consumerId!=0){
            consumerRepo.deleteDeliveryAddressById(addressId,consumerId);
            return consumerRepo.findConsumerByConsumerId(consumerId).getSetOfDeliveryAddress();
        }
        return Collections.emptySet();
    }




}

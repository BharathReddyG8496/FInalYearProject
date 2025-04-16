package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.DeliveryAddressesRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Utility.ConsumerRegisterDTO;
import com.example.finalyearproject.Utility.ConsumerUtility;
import com.example.finalyearproject.Utility.DeliveryAddressUtility;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private DeliveryAddressesRepo deliveryAddressesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

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

        String safeEmail = email.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        Path profileDir = Paths.get(uploadDir, "profiles", "consumers", safeEmail);
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

        return "/uploads/profiles/consumers/" + safeEmail + "/" + uniqueFilename;
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

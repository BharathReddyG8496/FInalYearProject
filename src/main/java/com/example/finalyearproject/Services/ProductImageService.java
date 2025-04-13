package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ProductImageRepository;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.DataStore.ProductImage;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
public class ProductImageService {


    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private  ProductRepo productRepo;

    // Inject the base directory for uploads from application.properties
    @Value("${app.upload.dir}")
    private String uploadDir;

    public void uploadProductImages(int productId, MultipartFile[] files) throws IOException, ResourceNotFoundException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));

        // Create a directory for the product if it doesn't exist
        Path productDir = Paths.get(uploadDir, "products", String.valueOf(productId));
        if (!Files.exists(productDir)) {
            Files.createDirectories(productDir);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            // Create a unique file name to avoid collisions
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save file to the file system
            Path filePath = productDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            // Create and save a ProductImage entity
            ProductImage image = new ProductImage();
            image.setFilename(uniqueFilename);
            // Save the relative file path; adjust as per your resource mapping later
            image.setFilePath("/uploads/products/" + productId + "/" + uniqueFilename);
            image.setProduct(product);

            // Add image to product images set
            product.getImages().add(image);
            productImageRepository.save(image);
        }

        // Save the updated product with its new images
        productRepo.save(product);
    }

    public void deleteAllImagesForProduct(int productId) throws IOException, ResourceNotFoundException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));

        // Delete each image file
        for (ProductImage image : product.getImages()) {
            Path filePath = Paths.get(uploadDir, "products", String.valueOf(productId), image.getFilename());
            Files.deleteIfExists(filePath);
        }

        // Optionally, remove the product directory if desired (and if empty)
        Path productDir = Paths.get(uploadDir, "products", String.valueOf(productId));
        if (Files.exists(productDir) && Files.list(productDir).findAny().isEmpty()) {
            Files.delete(productDir);
        }
    }
}

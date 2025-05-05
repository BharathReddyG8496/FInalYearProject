package com.example.finalyearproject.Services;

import com.cloudinary.Cloudinary;
import com.example.finalyearproject.Abstraction.ProductImageRepository;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.DataStore.ProductImage;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class ProductImageService {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageService.class);

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private Cloudinary cloudinary;

    public void uploadProductImages(int productId, MultipartFile[] files) throws IOException, ResourceNotFoundException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));

        List<String> successfullyUploadedIds = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // Validate image before upload
                if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                    logger.warn("Skipping non-image file for product {}: {}", productId, file.getOriginalFilename());
                    continue;
                }

                try {
                    // Prepare upload parameters for Cloudinary
                    String folderPath = "products/" + productId;
                    Map<String, Object> params = new HashMap<>();
                    params.put("folder", folderPath);
                    params.put("public_id", UUID.randomUUID().toString());
                    params.put("overwrite", true);
                    params.put("resource_type", "auto");

                    // Upload to Cloudinary
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

                    // Get the secure URL and public ID
                    String secureUrl = uploadResult.get("secure_url").toString();
                    String publicId = uploadResult.get("public_id").toString();

                    successfullyUploadedIds.add(publicId);

                    // Create and save a ProductImage entity
                    ProductImage image = new ProductImage();
                    image.setFilename(publicId);
                    image.setFilePath(secureUrl);
                    image.setProduct(product);

                    // Add image to product images set
                    product.getImages().add(image);
                    productImageRepository.save(image);

                    logger.info("Successfully uploaded image for product {}: {}", productId, publicId);

                } catch (IOException e) {
                    logger.error("Failed to upload image to Cloudinary for product {}: {}",
                            productId, e.getMessage(), e);
                    throw new IOException("Failed to upload image: " + e.getMessage(), e);
                } catch (RuntimeException e) {
                    logger.error("Cloudinary service error for product {}: {}",
                            productId, e.getMessage(), e);
                    throw new IOException("Cloud storage service error: " + e.getMessage(), e);
                }
            }

            // Save the updated product with its new images
            productRepo.save(product);

        } catch (Exception e) {
            // If overall process fails, clean up any successfully uploaded images
            logger.error("Error during product image upload process for product {}: {}",
                    productId, e.getMessage(), e);

            // Attempt to delete any images that were successfully uploaded to Cloudinary
            rollbackCloudinaryUploads(successfullyUploadedIds);

            // Re-throw the exception to inform the caller
            throw e;
        }
    }

    private void rollbackCloudinaryUploads(List<String> publicIds) {
        for (String publicId : publicIds) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("resource_type", "image");
                cloudinary.uploader().destroy(publicId, params);
                logger.info("Rolled back image upload: {}", publicId);
            } catch (Exception e) {
                logger.warn("Failed to roll back image upload for {}: {}", publicId, e.getMessage());
                // Continue with other deletions even if one fails
            }
        }
    }

    public void deleteAllImagesForProduct(int productId) throws ResourceNotFoundException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));

        boolean hasFailedDeletions = false;
        List<String> failedIds = new ArrayList<>();

        // Delete each image from Cloudinary
        for (ProductImage image : product.getImages()) {
            try {
                // Extract public_id from the filename field
                String publicId = image.getFilename();

                // Delete from Cloudinary
                Map<String, String> params = new HashMap<>();
                params.put("resource_type", "image");
                cloudinary.uploader().destroy(publicId, params);
                logger.info("Successfully deleted image {} for product {}", publicId, productId);

            } catch (IOException e) {
                hasFailedDeletions = true;
                failedIds.add(image.getFilename());
                logger.error("Failed to delete image {} for product {}: {}",
                        image.getFilename(), productId, e.getMessage(), e);
            } catch (RuntimeException e) {
                hasFailedDeletions = true;
                failedIds.add(image.getFilename());
                logger.error("Cloud service error while deleting image {} for product {}: {}",
                        image.getFilename(), productId, e.getMessage(), e);
            }
        }

        // Clear the product's images collection and save
        product.getImages().clear();
        productRepo.save(product);

        // Delete all product images from database
        productImageRepository.deleteByProduct_ProductId(productId);

        // Log warning if some deletions failed
        if (hasFailedDeletions) {
            logger.warn("Some images could not be deleted from cloud storage for product {}: {}",
                    productId, String.join(", ", failedIds));
        }
    }

    /**
     * Delete a single product image
     */
    public void deleteProductImage(int imageId) throws ResourceNotFoundException, IOException {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id " + imageId));

        try {
            // Delete from Cloudinary
            String publicId = image.getFilename();
            Map<String, String> params = new HashMap<>();
            params.put("resource_type", "image");
            cloudinary.uploader().destroy(publicId, params);

            // Remove from product's image collection
            Product product = image.getProduct();
            product.getImages().remove(image);

            // Delete from database
            productImageRepository.delete(image);

            logger.info("Successfully deleted image {} (id: {})", publicId, imageId);

        } catch (IOException e) {
            logger.error("Failed to delete image {} from cloud storage: {}",
                    image.getFilename(), e.getMessage(), e);
            throw new IOException("Failed to delete image from cloud storage: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.error("Cloud service error while deleting image {}: {}",
                    image.getFilename(), e.getMessage(), e);
            throw new IOException("Cloud storage service error: " + e.getMessage(), e);
        }
    }
}

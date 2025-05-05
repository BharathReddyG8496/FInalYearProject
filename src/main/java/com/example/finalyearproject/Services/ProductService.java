package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Utility.ProductUpdateDTO;
import com.example.finalyearproject.Utility.ProductUtility;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderItemRepo orderItemRepo;

    @Autowired
    private ProductImageService productImageService;



    @Transactional
    public Product AddProduct(ProductUtility prodUtil, String farmerEmail) {
        try {
            Product product = new Product();
            product.setName(prodUtil.getName());
            product.setDescription(prodUtil.getDescription());
            product.setPrice(prodUtil.getPrice());
            product.setStock(prodUtil.getStock());
            try {
                // Convert the String to CategoryType enum
                CategoryType category = CategoryType.valueOf(prodUtil.getCategory().toUpperCase());
                product.setCategory(category);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid category: " + prodUtil.getCategory());
            }
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            product.setFarmer(farmer);
            farmer.getFarmerProducts().add(product);
            Product storedProduct = productRepo.save(product);


            // If image files are provided, process the upload.
            if (prodUtil.getImages() != null && prodUtil.getImages().length > 0) {
                try {
                    productImageService.uploadProductImages(storedProduct.getProductId(), prodUtil.getImages());
                } catch (Exception e) {
                    // Here you could roll back the product insert or just return an error.
                    throw new RuntimeException(e);
                }
            }
            return storedProduct;

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Product updateProduct(ProductUpdateDTO dto, int productId, String farmerEmail) {
        // Retrieve the existing product
        Product existingProduct = productRepo.findProductByProductId(productId);
        if (existingProduct == null) {
            throw new RuntimeException("Product not found.");
        }

        // Retrieve the authenticated farmer
        Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
        if (farmer == null) {
            throw new RuntimeException("Farmer not found.");
        }

        // Optionally, verify that the product belongs to this farmer
        if (!existingProduct.getFarmer().getFarmerEmail().equalsIgnoreCase(farmerEmail)) {
            throw new RuntimeException("Unauthorized update attempt.");
        }

        // Save old values for later order adjustment
        double oldPrice = existingProduct.getPrice();
        int oldStock = existingProduct.getStock();

        // New values from DTO
        double newPrice = dto.getPrice();
        int newStock = dto.getStock();

        // Update product fields from DTO
        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(newPrice);
        existingProduct.setStock(newStock);
        existingProduct.setCategory(dto.getCategory());

        // Save updated product first
        productRepo.save(existingProduct);

        // Adjust associated order items (only for orders with status "CREATED")
        if (existingProduct.getOrderItems() != null && !existingProduct.getOrderItems().isEmpty()) {
            for (OrderItem item : existingProduct.getOrderItems()) {
                Order order = item.getOrder();
                if (!"CREATED".equals(order.getOrderStatus())) {
                    continue;
                }

                StringBuilder changeMsg = new StringBuilder();

                // Update price if changed
                if (oldPrice != newPrice) {
                    double delta = (newPrice - oldPrice) * item.getQuantity();
                    item.setUnitPrice(item.getUnitPrice() + delta);
                    order.setTotalAmount(order.getTotalAmount() + delta);
                    changeMsg.append(oldPrice < newPrice ? "Price Increased" : "Price Decreased");
                }

                // Update stock if changed and current order quantity exceeds the new stock
                if (oldStock != newStock && item.getQuantity() > newStock) {
                    int removedQty = item.getQuantity() - newStock;
                    double deduct = removedQty * newPrice;
                    item.setQuantity(newStock);
                    item.setUnitPrice(item.getUnitPrice() - deduct);
                    order.setTotalAmount(order.getTotalAmount() - deduct);
                    changeMsg.append(" | Stock Reduced");
                }

                item.setFieldChange(changeMsg.toString());
                orderItemRepo.save(item);
                orderRepo.save(order);
            }
        }

        // Return the updated product (if necessary, re-fetch from the repo)
        return productRepo.findProductByProductId(productId);
    }


    @Transactional
    public void DeleteProduct(int productId, String farmerEmail) {
        Farmer byFarmerEmail = farmerRepo.findByFarmerEmail(farmerEmail);
        try {
            // First, remove the image files from the file system.
            productImageService.deleteAllImagesForProduct(productId);
        } catch (ResourceNotFoundException ex) {
            System.err.println("Error deleting image files: " + ex.getMessage());
            // Depending on your needs, you might decide to cancel deletion or log and continue.
        }
//        // Now delete the product from the database.
        Product product = productRepo.findByFarmer_FarmerIdAndProductId(productId, byFarmerEmail.getFarmerId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID " + productId + " for farmer " + byFarmerEmail.getFarmerName()));
//
//        // Clear images from the product to trigger orphan removal.
//        product.getImages().clear();
//        productRepo.save(product);

        // Delete the product; now, child product_image rows will have been removed.
        productRepo.delete(product);
    }
}

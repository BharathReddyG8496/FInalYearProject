package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    public Product AddProduct(Product product, int farmerId, String categoryStr) {
        if (product == null || farmerId == 0 || categoryStr == null || categoryStr.isEmpty()) {
            throw new IllegalArgumentException("Product, Farmer ID, or category is missing");
        }

        Farmer farmer = farmerRepo.findByFarmerId(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + farmerId));
        product.setFarmer(farmer);

        try {
            // Convert the String to CategoryType enum
            CategoryType category = CategoryType.valueOf(categoryStr.toUpperCase());
            product.setCategory(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + categoryStr);
        }

        if (farmer.getFarmerProducts() == null) {
            farmer.setFarmerProducts(new HashSet<>());
        }
        farmer.getFarmerProducts().add(product);
        return productRepo.save(product);
    }

    public Product UpdateProduct(Product product,int productId, int farmerId){
        try {
            Product prevProduct = this.productRepo.findProductByProductId(productId);
            this.productRepo.updateProductById(product, productId,farmerId);
            String priceChange = (prevProduct.getPrice() > product.getPrice()) ? "DEC" :
                    (prevProduct.getPrice() < product.getPrice()) ? "INC" : "";
            double priceChangeVal = Math.abs(prevProduct.getPrice() - product.getPrice());
            String stockChange = (prevProduct.getStock() > product.getStock()) ? "DEC" :
                    (prevProduct.getStock() < product.getStock()) ? "INC" : "";
//        int stockChangeVal = Math.abs(prevProduct.getStock()- product.getStock());

            Set<OrderItem> orderItems = prevProduct.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getOrder().getOrderStatus().equals("CREATED")) {
                    Order order = orderItem.getOrder();
                    if (priceChange.equals("DEC")) {
                        orderItem.setFieldChange("The Price has been Decreased!!!");
                        orderItem.setUnitPrice(orderItem.getUnitPrice() - (priceChangeVal * orderItem.getQuantity()));
                        order.setTotalAmount(order.getTotalAmount() - (priceChangeVal * orderItem.getQuantity()));
                    } else if (priceChange.equals("INC")) {
                        orderItem.setFieldChange("The Price has been Increased!!!");
                        orderItem.setUnitPrice(orderItem.getUnitPrice() + (priceChangeVal * orderItem.getQuantity()));
                        order.setTotalAmount(order.getTotalAmount() + (priceChangeVal * orderItem.getQuantity()));
                    }
                    if (stockChange.equals("DEC")) {
                        orderItem.setFieldChange(orderItem.getFieldChange()+" AND The Stocks has been Decreased!!!");
                        if (orderItem.getQuantity() > product.getStock()) {
                            int stockChangeVal = Math.abs(orderItem.getQuantity() - product.getStock());
                            double priceChangeValForStock = stockChangeVal * product.getPrice();
                            orderItem.setQuantity(orderItem.getQuantity() - stockChangeVal);
                            orderItem.setUnitPrice(orderItem.getUnitPrice() - priceChangeValForStock);
                            order.setTotalAmount(order.getTotalAmount() - priceChangeValForStock);
                        }
                    } else if (stockChange.equals("INC")) {
                        orderItem.setFieldChange(orderItem.getFieldChange()+" AND The Stocks has been Increased!!! You can add still more products to your cart.");
                    }
                    this.orderItemRepo.save(orderItem);
                    this.orderRepo.save(order);
                }
            }

            return this.productRepo.findProductByProductId(productId);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Transactional
    public void DeleteProduct(int productId, int farmerId) {
        try {
            // First, remove the image files from the file system.
            productImageService.deleteAllImagesForProduct(productId);
        } catch (IOException | ResourceNotFoundException ex) {
            System.err.println("Error deleting image files: " + ex.getMessage());
            // Depending on your needs, you might decide to cancel deletion or log and continue.
        }
//        // Now delete the product from the database.
        Product product = productRepo.findByFarmer_FarmerIdAndProductId(productId, farmerId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID " + productId + " for farmer " + farmerId));
//
//        // Clear images from the product to trigger orphan removal.
//        product.getImages().clear();
//        productRepo.save(product);

        // Delete the product; now, child product_image rows will have been removed.
        productRepo.delete(product);
    }

}

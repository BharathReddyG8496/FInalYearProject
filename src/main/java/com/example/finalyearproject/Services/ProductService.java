package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Specifications.ProductSpecification;
import com.example.finalyearproject.Utility.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private ProductSessionManager sessionManager;

    @Autowired
    private ProductImageService productImageService;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Transactional
    public ApiResponse<ProductResponseDTO> AddProduct(ProductUtility prodUtil, String farmerEmail) {
        try {
            Product product = new Product();
            product.setName(prodUtil.getName());
            product.setDescription(prodUtil.getDescription());
            product.setPrice(prodUtil.getPrice());
            product.setStock(prodUtil.getStock());
            product.setHarvestDate(prodUtil.getHarvestDate());
            product.setAvailableFromDate(prodUtil.getAvailableFromDate());
            product.setOrganic(prodUtil.isOrganic());

            // Handle category
            try {
                CategoryType category = CategoryType.valueOf(prodUtil.getCategory().toUpperCase());
                product.setCategory(category);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Product creation failed", "Invalid category: " + prodUtil.getCategory());
            }

            // NEW: Handle unit
            try {
                Unit unit = Unit.valueOf(prodUtil.getUnit().toUpperCase());
                product.setUnit(unit);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Product creation failed", "Invalid unit: " + prodUtil.getUnit());
            }

            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Product creation failed", "Farmer not found with email: " + farmerEmail);
            }

            product.setFarmer(farmer);
            farmer.getFarmerProducts().add(product);
            Product storedProduct = productRepo.save(product);

            // Handle image uploads
            if (prodUtil.getImages() != null && prodUtil.getImages().length > 0) {
                try {
                    productImageService.uploadProductImages(storedProduct.getProductId(), prodUtil.getImages());
                } catch (Exception e) {
                    logger.error("Failed to upload product images: {}", e.getMessage(), e);
                }
            }
            // Convert to DTO before returning
            ProductResponseDTO responseDTO = convertToResponseDTO(storedProduct);
            return ApiResponse.success("Product added successfully", responseDTO);
        } catch (Exception e) {
            logger.error("Failed to add product: {}", e.getMessage(), e);
            return ApiResponse.error("Product creation failed", e.getMessage());
        }
    }
    @Transactional
    public ApiResponse<ProductResponseDTO> updateProduct(ProductUpdateDTO dto, int productId, String farmerEmail) {
        try {
            // Retrieve the existing product
            Product existingProduct = productRepo.findProductByProductId(productId);
            if (existingProduct == null) {
                return ApiResponse.error("Update failed", "Product not found with ID: " + productId);
            }

            // Retrieve the authenticated farmer
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Update failed", "Farmer not found with email: " + farmerEmail);
            }

            // Verify product ownership
            if (!existingProduct.getFarmer().getFarmerEmail().equalsIgnoreCase(farmerEmail)) {
                return ApiResponse.error("Update failed", "You don't have permission to update this product");
            }

            // Save old values for later order adjustment
            double oldPrice = existingProduct.getPrice();
            int oldStock = existingProduct.getStock();

            // Update product fields
            existingProduct.setName(dto.getName());
            existingProduct.setDescription(dto.getDescription());
            existingProduct.setPrice(dto.getPrice());
            existingProduct.setStock(dto.getStock());
            existingProduct.setCategory(dto.getCategory());
            existingProduct.setUnit(dto.getUnit()); // NEW: Update unit
            existingProduct.setHarvestDate(dto.getHarvestDate());
            existingProduct.setAvailableFromDate(dto.getAvailableFromDate());
            existingProduct.setOrganic(dto.isOrganic());
            existingProduct.setImages(existingProduct.getImages());

            productRepo.save(existingProduct);

            // Adjust associated order items (only for orders with status "CREATED")
            if (existingProduct.getOrderItems() != null && !existingProduct.getOrderItems().isEmpty()) {
                for (OrderItem item : existingProduct.getOrderItems()) {
                    Order order = item.getOrder();
                    if (!"CREATED".equals(order.getOrderStatus().toString())) {
                        continue;
                    }

                    StringBuilder changeMsg = new StringBuilder();

                    // Update price if changed
                    if (oldPrice != dto.getPrice()) {
                        double delta = (dto.getPrice() - oldPrice) * item.getQuantity();
                        item.setUnitPrice(item.getUnitPrice() + delta);
                        order.setTotalAmount(order.getTotalAmount() + delta);
                        changeMsg.append(oldPrice < dto.getPrice() ? "Price Increased" : "Price Decreased");
                    }

                    // Update stock if changed and current order quantity exceeds the new stock
                    if (oldStock != dto.getStock() && item.getQuantity() > dto.getStock()) {
                        int removedQty = item.getQuantity() - dto.getStock();
                        double deduct = removedQty * dto.getPrice();
                        item.setQuantity(dto.getStock());
                        item.setUnitPrice(item.getUnitPrice() - deduct);
                        order.setTotalAmount(order.getTotalAmount() - deduct);
                        changeMsg.append(" | Stock Reduced");
                    }

                    item.setFieldChange(changeMsg.toString());
                    orderItemRepo.save(item);
                    orderRepo.save(order);
                }
            }

            // Return updated product
            // Return updated product as DTO
            Product updatedProduct = productRepo.findProductByProductId(productId);
            ProductResponseDTO responseDTO = convertToResponseDTO(updatedProduct);
            return ApiResponse.success("Product updated successfully", responseDTO);
        } catch (Exception e) {
            logger.error("Failed to update product: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Void> DeleteProduct(int productId, String farmerEmail) {
        try {
            Farmer byFarmerEmail = farmerRepo.findByFarmerEmail(farmerEmail);
            if (byFarmerEmail == null) {
                return ApiResponse.error("Deletion failed", "Farmer not found with email: " + farmerEmail);
            }

            // Find the product
            Optional<Product> productOpt = productRepo.findByFarmer_FarmerIdAndProductId(byFarmerEmail.getFarmerId(),productId);
            if (productOpt.isEmpty()) {
                return ApiResponse.error("Deletion failed", "Product not found with ID " + productId +
                        " for farmer " + byFarmerEmail.getFarmerName());
            }

            // Handle image deletion - check the response instead of catching an exception
            ApiResponse<Void> imageDeleteResponse = productImageService.deleteAllImagesForProduct(productId);
            if (imageDeleteResponse.getErrors() != null) {
                // Log that image deletion had issues but continue with product deletion
                logger.warn("Issue with image deletion: {}", imageDeleteResponse.getMessage());
                // Continuing with product deletion despite image deletion issues
            }

            // Delete the product
            productRepo.delete(productOpt.get());
            return ApiResponse.success("Product deleted successfully");
        } catch (Exception e) {
            logger.error("Failed to delete product: {}", e.getMessage(), e);
            return ApiResponse.error("Deletion failed", e.getMessage());
        }
    }


    // Update getProductById method
    public ApiResponse<ProductResponseDTO> getProductById(int productId) {
        try {
            Optional<Product> productOpt = productRepo.findById(productId);
            if (productOpt.isPresent()) {
                ProductResponseDTO responseDTO = convertToResponseDTO(productOpt.get());
                return ApiResponse.success("Product retrieved successfully", responseDTO);
            } else {
                return ApiResponse.error("Product not found", "No product found with ID: " + productId);
            }
        } catch (Exception e) {
            logger.error("Failed to get product by ID: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve product", e.getMessage());
        }
    }

    /**
     * Get products by farmer ID
     */
    public ApiResponse<List<Product>> getProductsByFarmerId(int farmerId) {
        try {
            Optional<Farmer> farmerOpt = farmerRepo.findByFarmerId(farmerId);
            if (farmerOpt.isEmpty()) {
                return ApiResponse.error("Farmer not found", "No farmer found with ID: " + farmerId);
            }

            List<Product> products = productRepo.findByFarmer_FarmerId(farmerId);
            return ApiResponse.success("Products retrieved successfully", products);
        } catch (Exception e) {
            logger.error("Failed to get products by farmer ID: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve products", e.getMessage());
        }
    }

    /**
     * Get products by farmer email
     */
    public ApiResponse<List<Product>> getProductsByFarmerEmail(String farmerEmail) {
        try {
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Farmer not found", "No farmer found with email: " + farmerEmail);
            }

            List<Product> products = productRepo.findByFarmer_FarmerId(farmer.getFarmerId());
            return ApiResponse.success("Products retrieved successfully", products);
        } catch (Exception e) {
            logger.error("Failed to get products by farmer email: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve products", e.getMessage());
        }
    }

    /**
     * Get a product by ID and verify it belongs to a specific farmer
     */
    public ApiResponse<Product> getProductByIdAndFarmerEmail(int productId, String farmerEmail) {
        try {
            Product product = productRepo.findProductByProductId(productId);
            if (product == null) {
                return ApiResponse.error("Product not found", "No product found with ID: " + productId);
            }

            if (!product.getFarmer().getFarmerEmail().equals(farmerEmail)) {
                return ApiResponse.error("Unauthorized", "This product does not belong to the authenticated farmer");
            }

            return ApiResponse.success("Product retrieved successfully", product);
        } catch (Exception e) {
            logger.error("Failed to get product by ID and farmer email: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve product", e.getMessage());
        }
    }



    /**
     * Get featured products
     * This is a placeholder - implement your business logic for determining featured products
     */
    public ApiResponse<List<Product>> getFeaturedProducts() {
        try {
            // This is just an example implementation
            // You might want to implement different logic based on your requirements
            // For example, products with highest ratings, most ordered, etc.
            List<Product> products = productRepo.findTop10ByOrderByAverageRatingDesc();
            return ApiResponse.success("Featured products retrieved successfully", products);
        } catch (Exception e) {
            logger.error("Failed to get featured products: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve featured products", e.getMessage());
        }
    }

    /**
     * Get recently added products
     */
    public ApiResponse<List<Product>> getRecentProducts() {
        try {
            // Assuming you have a createdAt field or similar to sort by
            // If not, you'll need to adjust this method or add such a field to your Product entity
            List<Product> products = productRepo.findTop10ByOrderByProductIdDesc(); // Using ID as a proxy for recency
            return ApiResponse.success("Recent products retrieved successfully", products);
        } catch (Exception e) {
            logger.error("Failed to get recent products: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve recent products", e.getMessage());
        }
    }

    /**
     * Reset the random order (can be called when a user explicitly wants a new shuffle)
     */
    public ApiResponse<String> resetRandomOrder() {
        try {
            List<Integer> allProductIds = productRepo.findAllAvailableProductIds();
            sessionManager.setShuffledProductIds(allProductIds);
            return ApiResponse.success("Random product order has been reset", null);
        } catch (Exception e) {
            logger.error("Failed to reset random order: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to reset random order", e.getMessage());
        }
    }
    // Update getProducts method (for paginated results)
    public ApiResponse<Page<ProductResponseDTO>> getProducts(ProductFilterDTO filterDTO) {
        try {
            boolean hasFilters = isFilterApplied(filterDTO);

            if (!hasFilters && filterDTO.getSortBy() == null) {
                return getRandomProductsPaginatedWithDTO(filterDTO);
            } else {
                return getFilteredProductsWithDTO(filterDTO);
            }
        } catch (Exception e) {
            logger.error("Failed to get products: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve products", e.getMessage());
        }
    }

    private boolean isFilterApplied(ProductFilterDTO filterDTO) {
        return filterDTO.getMinPrice() != null ||
                filterDTO.getMaxPrice() != null ||
                filterDTO.getCategory() != null ||
                filterDTO.getSearchTerm() != null ||
                filterDTO.getIsOrganic() != null ||
                filterDTO.getFarmerId() != null ||
                filterDTO.getMinRating() != null;
    }

    // New method for random products with DTO
    private ApiResponse<Page<ProductResponseDTO>> getRandomProductsPaginatedWithDTO(ProductFilterDTO filterDTO) {
        try {
            if (!sessionManager.hasShuffledIds()) {
                List<Integer> allProductIds = productRepo.findAllAvailableProductIds();
                sessionManager.setShuffledProductIds(allProductIds);
            }

            List<Integer> pageProductIds = sessionManager.getPageOfIds(
                    filterDTO.getPage(),
                    filterDTO.getSize());

            if (pageProductIds.isEmpty()) {
                Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize());
                return ApiResponse.success(
                        "No more products available",
                        new PageImpl<>(Collections.emptyList(), pageable, sessionManager.getShuffledProductIds().size())
                );
            }

            List<Product> products = productRepo.findByProductIdsIn(pageProductIds);
            Map<Integer, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, Function.identity()));

            List<ProductResponseDTO> orderedProducts = pageProductIds.stream()
                    .map(productMap::get)
                    .filter(Objects::nonNull)
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize());
            Page<ProductResponseDTO> productPage = new PageImpl<>(
                    orderedProducts,
                    pageable,
                    sessionManager.getShuffledProductIds().size()
            );

            return ApiResponse.success("Random products retrieved successfully", productPage);
        } catch (Exception e) {
            logger.error("Failed to get random products: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve random products", e.getMessage());
        }
    }

    // New method for filtered products with DTO
    public ApiResponse<Page<ProductResponseDTO>> getFilteredProductsWithDTO(ProductFilterDTO filterDTO) {
        try {
            Specification<Product> spec = ProductSpecification.getFilteredProducts(filterDTO);
            Sort sort = Sort.unsorted();

            if (filterDTO.getSortBy() != null) {
                sort = switch (filterDTO.getSortBy()) {
                    case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
                    case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
                    case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
                    case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
                    case "date_asc" -> Sort.by(Sort.Direction.ASC, "availableFromDate");
                    case "date_desc" -> Sort.by(Sort.Direction.DESC, "availableFromDate");
                    default -> Sort.by(Sort.Direction.DESC, "productId");
                };
            }

            Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);
            Page<Product> products = productRepo.findAll(spec, pageable);

            Page<ProductResponseDTO> productDTOPage = products.map(this::convertToResponseDTO);

            return ApiResponse.success("Filtered products retrieved successfully", productDTOPage);
        } catch (Exception e) {
            logger.error("Failed to filter products: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to filter products", e.getMessage());
        }
    }


    private ProductResponseDTO convertToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .harvestDate(product.getHarvestDate())
                .availableFromDate(product.getAvailableFromDate())
                .isOrganic(product.isOrganic())
                .category(product.getCategory())
                .unit(product.getUnit())
                .averageRating(product.getAverageRating())
                .ratingCount(product.getRatingCount())
                // Farmer details
                .farmerId(product.getFarmer().getFarmerId())
                .farmerName(product.getFarmer().getFirstName() + " " + product.getFarmer().getLastName())
                .farmerEmail(product.getFarmer().getFarmerEmail())
                .farmerPhone(product.getFarmer().getFarmerPhone())
                .farmerRating(product.getFarmer().getAverageRating())
                // Images
                .images(product.getImages().stream()
                        .map(img -> new ProductImageDTO(img.getId(), img.getFilename(), img.getFilePath()))
                        .collect(Collectors.toSet()))
                .build();
    }

}
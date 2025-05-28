package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Mappers.OrderMapper;
import com.example.finalyearproject.Services.FarmerService;
import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.Utility.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/farmer")
public class FarmerController {

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RatingServices ratingServices;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    /*
     * =======================================
     * FARMER PROFILE ENDPOINTS
     * =======================================
     */
    /**
     * Get farmer profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Farmer>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        ApiResponse<Farmer> response = farmerService.getFarmerByEmail(email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Update farmer profile - keep your existing endpoint
     */
    @PutMapping("/update-profile")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<FarmerUtility>> updateFarmer(
            @Valid @RequestBody FarmerUpdateDTO updateDTO,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<FarmerUtility> response = farmerService.updateFarmer(updateDTO, farmerEmail);

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Update profile photo
     */
    @PostMapping("/profile/photo")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<String>> updateProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String email = authentication.getName();
        ApiResponse<String> response = farmerService.updateProfilePhoto(file, email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Get farmer's performance statistics
     */
    @GetMapping("/profile/stats")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFarmerStats(
            Authentication authentication) {

        String email = authentication.getName();
        ApiResponse<Map<String, Object>> response = farmerService.getFarmerStats(email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    /*
     * =======================================
     * FARMER PRODUCT ENDPOINTS
     * =======================================
     */

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Set<ProductResponseUtility>>> GetAllProductsFormatted() {
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        ApiResponse<List<Product>> productsResponse = productService.getProductsByFarmerEmail(farmerEmail);

        if (productsResponse.getData() != null) {
            Set<ProductResponseUtility> formattedProducts = productsResponse.getData().stream()
                    .map(product -> productService.mapToProductResponseUtility(product))
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", formattedProducts));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve products", productsResponse.getMessage()));
        }
    }

    @GetMapping("product/{productId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponseUtility>> getProduct(
            @PathVariable int productId,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<Product> productResponse = productService.getProductByIdAndFarmerEmail(productId, farmerEmail);

        if (productResponse.getData() != null) {
            ProductResponseUtility formattedProduct = productService.mapToProductResponseUtility(productResponse.getData());
            return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", formattedProduct));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Product not found", productResponse.getMessage())
        );
    }


    @GetMapping("/ratings/{productId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Set<Rating>>> getProductRatings(@PathVariable int productId) {
        try {
            ApiResponse<Set<Rating>> response = ratingServices.getProductRatings(productId);

            if (response.getData() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve ratings", e.getMessage()));
        }
    }

    /*
     * =======================================
     * FARMER ORDER ENDPOINTS
     * =======================================
     */

    /**
     * Get all orders containing products from this farmer
     * FILTERED: Returns only this farmer's items
     */
    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> getFarmerOrders(Authentication authentication) {
        String farmerEmail = authentication.getName();
        ApiResponse<List<Order>> response = orderService.getFarmerFilteredOrders(farmerEmail);

        if (response.isSuccess()) {
            List<OrderSummaryDTO> orderSummaries = response.getData().stream()
                    .map(orderMapper::toOrderSummaryDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), orderSummaries));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Get specific order details (only this farmer's items)
     * FILTERED: Returns only this farmer's items
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getFarmerOrderDetails(
            @PathVariable int orderId,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<Order> response = orderService.getFarmerOrderDetails(orderId, farmerEmail);

        if (response.getData() != null) {
            OrderResponseDTO orderDTO = orderMapper.toOrderResponseDTO(response.getData());
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), orderDTO));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Get orders by item status
     * FILTERED: Returns only this farmer's items
     */
    @GetMapping("/orders/by-status/{status}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> getFarmerOrdersByItemStatus(
            @PathVariable FulfillmentStatus status,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<List<Order>> response = orderService.getFarmerOrdersByItemStatus(farmerEmail, status);

        if (response.isSuccess()) {
            List<OrderSummaryDTO> orderSummaries = response.getData().stream()
                    .map(orderMapper::toOrderSummaryDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), orderSummaries));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Get all items sold by this farmer across all orders
     */
    @GetMapping("/order-items")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<OrderItemResponseDTO>>> getAllFarmerItems(Authentication authentication) {
        String farmerEmail = authentication.getName();
        ApiResponse<List<OrderItem>> response = orderService.getAllFarmerItems(farmerEmail);

        if (response.isSuccess()) {
            List<OrderItemResponseDTO> itemDTOs = response.getData().stream()
                    .map(orderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), itemDTOs));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Mark farmer's items as delivered
     */
    @PutMapping("/orders/{orderId}/items/deliver")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<OrderItemResponseDTO>>> markItemsDelivered(
            @PathVariable int orderId,
            @RequestBody(required = false) DeliveryNotesDTO notesDTO,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        String deliveryNotes = notesDTO != null ? notesDTO.getDeliveryNotes() : null;

        ApiResponse<List<OrderItem>> response = orderService.markFarmerItemsDelivered(
                orderId, farmerEmail, deliveryNotes);

        if (response.isSuccess()) {
            List<OrderItemResponseDTO> itemDTOs = response.getData().stream()
                    .map(orderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), itemDTOs));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }
    /**
     * Farmer cancels their own items
     */
    @PutMapping("/orders/{orderId}/items/cancel")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<OrderItemResponseDTO>>> cancelItems(
            @PathVariable int orderId,
            @RequestBody OrderItemStatusUpdateDTO updateDTO,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<List<OrderItem>> response = orderService.cancelOrderItems(
                orderId, updateDTO.getOrderItemIds(), farmerEmail, "FARMER", updateDTO.getNotes());

        if (response.isSuccess()) {
            List<OrderItemResponseDTO> itemDTOs = response.getData().stream()
                    .map(orderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), itemDTOs));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    @GetMapping("/units")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<UnitDTO>>> getAvailableUnits() {
        try {
            List<UnitDTO> units = Arrays.stream(Unit.values())
                    .map(unit -> new UnitDTO(unit.name(), unit.getDisplayName()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Available units retrieved successfully", units));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve units", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAvailableCategories() {
        try {
            List<CategoryDTO> categories = Arrays.stream(CategoryType.values())
                    .map(category -> new CategoryDTO(
                            category.name(),
                            category.getDisplayName()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Available categories retrieved successfully", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve categories", e.getMessage()));
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> exceptionHandler() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", "Invalid credentials"));
    }
}
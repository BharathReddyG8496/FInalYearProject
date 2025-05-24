package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.FulfillmentStatus;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer> {
    // Find active cart (order with CREATED status) for a consumer
    @Query("SELECT o FROM Order o WHERE o.consumer.consumerId = :consumerId AND o.orderStatus = com.example.finalyearproject.DataStore.OrderStatus.CREATED")
    Order findActiveCartByConsumerId(@Param("consumerId") int consumerId);

    // Find consumer's orders
    List<Order> findByConsumer_ConsumerIdOrderByCreatedAtDesc(int consumerId);

    // Find orders containing farmer's products
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.farmer f " +
            "WHERE f.farmerEmail = :farmerEmail AND (o.orderStatus = com.example.finalyearproject.DataStore.OrderStatus.PLACED " +
            "OR o.orderStatus = com.example.finalyearproject.DataStore.OrderStatus.DELIVERED)")
    List<Order> findPlacedOrdersContainingFarmerProducts(@Param("farmerEmail") String farmerEmail);

    // Find orders with specific items in specific fulfillment status
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.farmer f " +
            "WHERE f.farmerEmail = :farmerEmail AND oi.fulfillmentStatus = :status")
    List<Order> findOrdersByFarmerAndItemStatus(@Param("farmerEmail") String farmerEmail,
                                                @Param("status") FulfillmentStatus status);

    // Find orders with at least one item in PENDING status
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi " +
            "WHERE (o.orderStatus = com.example.finalyearproject.DataStore.OrderStatus.PLACED " +
            "OR o.orderStatus = com.example.finalyearproject.DataStore.OrderStatus.DELIVERED) " +
            "AND oi.fulfillmentStatus = com.example.finalyearproject.DataStore.FulfillmentStatus.PENDING")
    List<Order> findOrdersWithPendingItems();

    // Find all orders for a specific consumer
    List<Order> findByConsumer_ConsumerEmailOrderByCreatedAtDesc(String consumerEmail);

    // Find all orders containing products from a specific farmer regardless of status
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.farmer f " +
            "WHERE f.farmerEmail = :farmerEmail")
    List<Order> findAllOrdersContainingFarmerProducts(@Param("farmerEmail") String farmerEmail);

    /**
     * Search orders with various filters
     */
    @Query("SELECT o FROM Order o WHERE o.consumer.consumerId = :consumerId " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:dateFrom IS NULL OR o.placedAt >= :dateFrom) " +
            "AND (:dateTo IS NULL OR o.placedAt <= :dateTo) " +
            "ORDER BY o.placedAt DESC")
    List<Order> searchOrders(@Param("consumerId") int consumerId,
                             @Param("status") OrderStatus status,
                             @Param("dateFrom") java.util.Date dateFrom,
                             @Param("dateTo") java.util.Date dateTo);
}
package com.example.finalyearproject.Abstraction;
import com.example.finalyearproject.DataStore.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Integer> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.consumer.consumerId = :consumerId " +
            "AND oi.orderItemId = :orderItemId AND oi.order.orderStatus = com.example.finalyearproject.DataStore.OrderStatus.CREATED")
    OrderItem findOrderItemWithStatusCREATED(@Param("consumerId") int consumerId,
                                             @Param("orderItemId") int orderItemId);

    // Find items by product ID and consumer ID (used by RatingServices)
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.productId = :productId " +
            "AND oi.order.consumer.consumerId = :consumerId")
    List<OrderItem> findByProductIdAndConsumerId(@Param("productId") int productId,
                                                 @Param("consumerId") int consumerId);

    // Find items by farmer email
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.farmer.farmerEmail = :farmerEmail")
    List<OrderItem> findByFarmerEmail(@Param("farmerEmail") String farmerEmail);

    // Find items for a specific order and farmer
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId " +
            "AND oi.product.farmer.farmerEmail = :farmerEmail")
    Set<OrderItem> findByOrderIdAndFarmerEmail(@Param("orderId") int orderId,
                                               @Param("farmerEmail") String farmerEmail);

    // Find items by order ID and item IDs
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId " +
            "AND oi.orderItemId IN :itemIds")
    List<OrderItem> findByOrderIdAndItemIds(@Param("orderId") int orderId,
                                            @Param("itemIds") List<Integer> itemIds);
}
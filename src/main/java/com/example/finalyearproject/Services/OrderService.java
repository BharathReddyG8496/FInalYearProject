package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Utility.OrderUtility;
//import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class OrderService {

    @Autowired
    private OrderItemRepo orderItemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private ProductRepo productRepo;

    public OrderUtility AddToCart(int consumerId, int productId, int quantity){
        try{
        if(consumerId==0 || productId==0 || quantity==0){
            return new OrderUtility(400,"The parameters cannot be null!!!",null);
        }
        Consumer consumer = this.consumerRepo.findConsumerByConsumerId(consumerId);
        Product product = this.productRepo.findProductByProductId(productId);
        if(consumer==null || product==null){
            return new OrderUtility(400,"Unable to find the consumer or product in DB",null);
        }
        Order order;
        OrderItem orderItem;
        var testing = consumer.getConsumerOrder();
        if(consumer.getConsumerOrder().isEmpty()){
            Set<Order> orders = new HashSet<>();
            order = new Order();
            order.setConsumer(consumer);
            order.setOrderStatus("CREATED");
            Set<OrderItem> orderItems = new HashSet<>();
            orderItem = new OrderItem();
            if(quantity>product.getStock())
                return new OrderUtility(400,"The entered quantity is exceeding the Stock of Product", null);


            order.setTotalAmount(product.getPrice()*quantity);
            orderItems.add(orderItem);
            order.setOrderItems(orderItems);
            orderRepo.save(order);

            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
//            product.setStock(product.getStock()-quantity);
            orderItem.setUnitPrice(product.getPrice()*quantity);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            if(product.getOrderItem() == null){
                Set<OrderItem> orderItemsSet = new HashSet<>();
                orderItemsSet.add(orderItem);
                product.setOrderItem(orderItemsSet);
            }else{
                product.getOrderItem().add(orderItem);
            }

            orderItemRepo.save(orderItem);

            orders.add(order);
            consumer.setConsumerOrder(orders);
            return new OrderUtility(200,"Items completely added to the Cart",orderItems);
        }else{
            for(Order order1 : consumer.getConsumerOrder()){
                if(order1.getOrderStatus().equals("CREATED")){

                    // Checks for existing product in the cart.
                    for(OrderItem orderItem1: order1.getOrderItems()){
                        if(orderItem1.getProduct().getProductId()==productId) {
                            if((orderItem1.getQuantity()+quantity)>product.getStock())
                                return new OrderUtility(400,"The entered quantity is exceeding the Stock of Product", null);

                            orderItem1.setQuantity(orderItem1.getQuantity()+quantity);
                            orderItem1.setUnitPrice(orderItem1.getUnitPrice()+(product.getPrice()*quantity));
                            this.orderItemRepo.save(orderItem1);
                            order1.setTotalAmount(order1.getTotalAmount()+(product.getPrice()*quantity));

                            this.orderRepo.save(order1);
                            return new OrderUtility(200,"Added the items to the existing order",order1.getOrderItems());
                        }
                    }

                    // Adds new product into the cart.
                    orderItem = new OrderItem();
                    if(quantity>product.getStock())
                        return new OrderUtility(400,"The entered quantity is exceeding the Stock of Product", null);
                    orderItem.setProduct(product);

                    if(product.getOrderItem() == null){
                        Set<OrderItem> orderItemsSet = new HashSet<>();
                        orderItemsSet.add(orderItem);
                        product.setOrderItem(orderItemsSet);
                    }else{
                        product.getOrderItem().add(orderItem);
                    }
                    orderItem.setQuantity(quantity);
//            product.setStock(product.getStock()-quantity);
                    orderItem.setUnitPrice(product.getPrice()*quantity);
                    orderItem.setOrder(order1);
                    orderItem.setProduct(product);
                    orderItemRepo.save(orderItem);

                    order1.setTotalAmount(order1.getTotalAmount()+(product.getPrice()*quantity));
                    order1.getOrderItems().add(orderItem);
                    this.orderRepo.save(order1);
                    return new OrderUtility(200,"New item added to the cart",order1.getOrderItems());
                }

            }

        }
        return new OrderUtility(400,"Something went wrong",null);
    }catch(Exception e){
        return new OrderUtility(400, e.getMessage(), null);
    }
    }

    public OrderUtility RemoveFromCart(int consumerId,int orderItemId,int quantity){

        try {
            Consumer consumer = this.consumerRepo.findConsumerByConsumerId(consumerId);
            if (consumerId == 0 || orderItemId == 0 || consumer == null) {
                return new OrderUtility(400, "The parameters cannot be null!!!", null);
            }

            OrderItem orderItem = this.orderItemRepo.findOrderItemWithStatusCREATED(consumerId, orderItemId);
            Order order = this.orderRepo.findByStatusAndConsumerId("CREATED", consumerId);
            Product product = orderItem.getProduct();
            if (quantity >= orderItem.getQuantity()) {
                order.setTotalAmount(order.getTotalAmount() - orderItem.getUnitPrice());
                order.getOrderItems().remove(orderItem);
                product.getOrderItem().remove(orderItem);
                this.orderItemRepo.deleteByOrderItemId(orderItemId);
                return new OrderUtility(200, "Removed the product from the cart", order.getOrderItems());
            }
            double priceChange = (quantity * orderItem.getProduct().getPrice());
            orderItem.setQuantity(orderItem.getQuantity() - quantity);
            orderItem.setUnitPrice(orderItem.getUnitPrice() - priceChange);
            order.setTotalAmount(order.getTotalAmount() - priceChange);
            this.orderItemRepo.save(orderItem);
            this.orderRepo.save(order);
            return new OrderUtility(200, "Removed the specific quantity of products", order.getOrderItems());
        }catch(Exception e){
            return new OrderUtility(400, e.getMessage(), null);
        }
    }

    public OrderUtility GetConsumerCart(int consumerId){
        if(consumerId==0){
            return new OrderUtility(400,"ConsumerId is 0",null);
        }
        Set<OrderItem> orderItemSet = this.orderRepo.getConsumersCart(consumerId).getOrderItems();
        if(orderItemSet!=null)
            return new OrderUtility(200,"Total Products : "+orderItemSet.size(),orderItemSet);
        return new OrderUtility(400,"Got null set from the executed query",null);
    }

    public OrderUtility UpdateTheChangesField(int consumerId){
        if(consumerId==0){
            return new OrderUtility(400,"ConsumerId is 0",null);
        }
        Set<OrderItem> orderItemSet = this.orderRepo.getConsumersCart(consumerId).getOrderItems();
        if(orderItemSet!=null) {
            for(OrderItem orderItem : orderItemSet){
                if(orderItem.getFieldChange()!=null) {
                    orderItem.setFieldChange(null);
                    this.orderItemRepo.save(orderItem);
                }
            }
            return new OrderUtility(200, "Total Products : " + orderItemSet.size(), orderItemSet);
        }
        return new OrderUtility(400,"Got null set from the executed query",null);

    }


}

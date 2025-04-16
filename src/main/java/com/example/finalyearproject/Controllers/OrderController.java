package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Utility.OrderUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ConsumerRepo consumerRepo;

    @PostMapping("/add-to-cart/{productId}/{quantity}")
    public ResponseEntity<OrderUtility> AddToCart(@PathVariable int productId, @PathVariable int quantity){
        Consumer consumer = consumerRepo.findByConsumerEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        OrderUtility orderUtility = this.orderService.AddToCart(consumer.getConsumerId(),productId,quantity);
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

    @PostMapping("/remove-from-cart/{orderItemId}/{quantity}")
    public ResponseEntity<OrderUtility> RemoveFromCart(@PathVariable int orderItemId,@PathVariable int quantity){
        Consumer consumer = consumerRepo.findByConsumerEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        OrderUtility orderUtility = this.orderService.RemoveFromCart(consumer.getConsumerId(), orderItemId, quantity);
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

    @GetMapping("/get-consumer-cart")
    public ResponseEntity<OrderUtility> GetConsumerCart(){
        Consumer consumer = consumerRepo.findByConsumerEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        OrderUtility orderUtility = this.orderService.GetConsumerCart(consumer.getConsumerId());
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

    @GetMapping("/update-the-ChangesField")
    public ResponseEntity<OrderUtility> UpdateTheChangesField(){
        Consumer consumer = consumerRepo.findByConsumerEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        OrderUtility orderUtility = this.orderService.UpdateTheChangesField(consumer.getConsumerId());
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

}

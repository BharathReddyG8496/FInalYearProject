package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Utility.OrderUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/add-to-cart/{consumerId}/{productId}/{quantity}")
    public ResponseEntity<OrderUtility> AddToCart(@PathVariable int consumerId,@PathVariable int productId, @PathVariable int quantity){
        OrderUtility orderUtility = this.orderService.AddToCart(consumerId,productId,quantity);
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

    @PostMapping("/remove-from-cart/{consumerId}/{orderItemId}/{quantity}")
    public ResponseEntity<OrderUtility> RemoveFromCart(@PathVariable int consumerId, @PathVariable int orderItemId,@PathVariable int quantity){
        OrderUtility orderUtility = this.orderService.RemoveFromCart(consumerId, orderItemId, quantity);
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

    @GetMapping("/get-consumer-cart/{consumerId}")
    public ResponseEntity<OrderUtility> GetConsumerCart(@PathVariable int consumerId){
        OrderUtility orderUtility = this.orderService.GetConsumerCart(consumerId);
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

    @GetMapping("/update-the-ChangesField/{consumerId}")
    public ResponseEntity<OrderUtility> UpdateTheChangesField(@PathVariable int consumerId){
        OrderUtility orderUtility = this.orderService.UpdateTheChangesField(consumerId);
        if(orderUtility.getOrderItems()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(orderUtility);
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderUtility);
    }

}

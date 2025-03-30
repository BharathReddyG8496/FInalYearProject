package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Utility.OrderUtility;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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

}

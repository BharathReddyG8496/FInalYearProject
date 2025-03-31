package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.DataStore.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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

    public Product AddProduct(Product product, int farmerId){
        if (product == null || farmerId == 0) {
            throw new IllegalArgumentException("Product or Farmer ID cannot be null/zero");
        }
        Farmer farmer = farmerRepo.findFarmerByFarmerId(farmerId);
        if (farmer == null) {
            throw new RuntimeException("Farmer not found with ID: " + farmerId);
        }
        product.setFarmer(farmer);
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

            Set<OrderItem> orderItems = prevProduct.getOrderItem();
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

            Product product1 = this.productRepo.findProductByProductId(productId);
            return product1;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void DeleteProduct(int productId,int farmerId){

        this.productRepo.deleteByProductId(productId, farmerId);
    }

}

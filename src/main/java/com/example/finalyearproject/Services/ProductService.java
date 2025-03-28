package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Farmer;
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

    public Product UpdateProduct(Product product, int farmerId){
        this.productRepo.updateProductById(product, farmerId);

        // Still more  modifications might require....

        Product product1 = this.productRepo.findProductByProductId(product.getProductId());
        return product1;
    }

    public void DeleteProduct(int productId,int farmerId){

        this.productRepo.deleteByProductId(productId, farmerId);
    }

}

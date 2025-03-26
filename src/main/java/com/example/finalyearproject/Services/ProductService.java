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
        Farmer farmer = this.farmerRepo.findFarmerByFarmerId(farmerId);
        if(farmer!=null){
            if(farmer.getFarmerProducts()==null){
                Set<Product> products = new HashSet<>();
                products.add(product);
                farmer.setFarmerProducts(products);
            }else{
                farmer.getFarmerProducts().add(product);
            }
            product.setFarmer(farmer);
            return this.productRepo.save(product);
        }
        return null;
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

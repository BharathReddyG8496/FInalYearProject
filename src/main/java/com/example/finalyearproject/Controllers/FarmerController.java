package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Services.FarmerService;
import com.example.finalyearproject.Services.ProductService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/farmer")
public class FarmerController {

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepo productRepo;

    @PostMapping("/register-farmer")
    public ResponseEntity<Farmer> RegisterFarmer(@Valid @RequestBody Farmer farmer){
        if(farmer==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(this.farmerService.RegisterFarmer(farmer));
    }

    @RequestMapping(value = "/login-farmer",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        this.doAuthenticate(request.getUserEmail(), request.getUserPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserEmail());
        String userName="";
        try{
            userName = this.farmerRepo.findFarmerByFarmerEmail(request.getUserEmail()).orElseThrow().getFarmerName();

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.valueOf("Incorrect credentials")).build();
        }
        String token = this.jwtHelper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .userName(userName).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // update-farmer
    @PutMapping("/update-farmer/{farmerId}")
    public ResponseEntity<Farmer> UpdateProduct(@Valid @RequestBody Farmer farmer, @PathVariable("farmerId")int farmerId){
        if(farmerId!=0){
            Farmer farmer1 = this.farmerService.UpdateFarmer(farmer,farmerId);
            if(farmer1!=null)
                return ResponseEntity.ok(farmer1);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/get-all-products/{farmerId}")
    public ResponseEntity<Set<Product>> GetAllProducts(@PathVariable("farmerId")int farmerId){
        Optional<Farmer> byFarmerId = farmerRepo.findByFarmerId(farmerId);
        return byFarmerId.map(farmer -> new ResponseEntity<>(farmer.getFarmerProducts(), HttpStatus.OK)).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());

    }

    private void doAuthenticate(String userEmail, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userEmail, password);
        try {
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Admin_name or Password  !!");
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }


}

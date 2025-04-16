package com.example.finalyearproject.Controllers;
import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Rating;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Services.FarmerService;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.Utility.ConsumerRegisterDTO;
import com.example.finalyearproject.Utility.ConsumerUtility;
import com.example.finalyearproject.Utility.FarmerRegisterDTO;
import com.example.finalyearproject.Utility.FarmerUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/public")
public class PublicController {


    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private RatingServices ratingServices;

    @PostMapping(path = "/create-consumer")
    public ResponseEntity<ConsumerUtility> RegisterConsumer(@Valid @ModelAttribute ConsumerRegisterDTO consumerRegisterDTO){
        ConsumerUtility consumer1 = this.consumerService.RegisterConsumer(consumerRegisterDTO);
        if(consumer1!=null)
            return ResponseEntity.ok(consumer1);
        return new ResponseEntity<>(new ConsumerUtility(400,"Failed to register consumer", null),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/login-consumer",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseEntity<JwtResponse> loginConsumer(@RequestBody JwtRequest request) {

        this.doAuthenticate(request.getUserEmail(), request.getUserPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserEmail());
        String token = this.jwtHelper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .userName(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    private void doAuthenticate(String userName, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName, password);
        try {
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Admin_name or Password  !!");
        }

    }

    @PostMapping("/register-farmer")
    public ResponseEntity<FarmerUtility> RegisterFarmer(@Valid @ModelAttribute FarmerRegisterDTO farmerRegisterDTO){
        FarmerUtility farmerUtility = farmerService.RegisterFarmer(farmerRegisterDTO);
        if(farmerUtility!=null){
            return ResponseEntity.ok(farmerUtility);
        }else {
            return new ResponseEntity<>(new FarmerUtility(400, "Failed to register", null),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/login-farmer",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseEntity<JwtResponse> loginFarmer(@RequestBody JwtRequest request) {

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

    @GetMapping("/get-product-ratings/{productId}")
    public ResponseEntity<?> getProductRatings(@PathVariable int productId){
        try {
            Set<Rating> productRatings = ratingServices.getProductRatings(productId);
            return ResponseEntity.ok(productRatings);
        }catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

}

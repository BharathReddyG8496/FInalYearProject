package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
import com.example.finalyearproject.Security.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

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

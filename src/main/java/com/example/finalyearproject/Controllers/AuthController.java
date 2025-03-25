package com.example.finalyearproject.Controllers;


import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Services.ConsumerService;
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

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AuthenticationManager manager;


    @GetMapping("/getUser")
    public ResponseEntity<String> getUser(){
        return new ResponseEntity<>("Hello",HttpStatus.ACCEPTED);
    }


    @PostMapping("/create-consumer")
    public ResponseEntity<Consumer> RegisterConsumer(@RequestBody Consumer consumer){
        Consumer consumer1 = this.consumerService.RegisterConsumer(consumer);
        if(consumer1!=null)
            return ResponseEntity.ok(consumer1);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping(value = "/login-consumer",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        this.doAuthenticate(request.getConsumerName(), request.getConsumerPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getConsumerName());
        String token = this.jwtHelper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .consumerName(userDetails.getUsername()).build();
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

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }



}

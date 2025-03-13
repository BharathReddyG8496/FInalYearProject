package com.example.finalyearproject.Controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AuthController {

    @GetMapping("/getUser")
    public ResponseEntity<String> getUser(){
        return new ResponseEntity<>("Hello",HttpStatus.ACCEPTED);
    }

   


}

package com.example.finalyearproject.Controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AuthController {

    @GetMapping("/getUser")
    public ResponseEntity<Object> getUser(){
        return new ResponseEntity<>("What are you doing,Im a user",HttpStatus.ACCEPTED);
    }


}

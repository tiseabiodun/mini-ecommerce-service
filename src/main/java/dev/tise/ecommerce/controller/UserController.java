package dev.tise.ecommerce.controller;

import dev.tise.ecommerce.model.User;
import dev.tise.ecommerce.request.UpdateBalanceRequest;
import dev.tise.ecommerce.request.UserRequest;
import dev.tise.ecommerce.request.ValidateUserRequest;
import dev.tise.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/ecommerce/user")
public class UserController {
    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //todo: Role application

    @PostMapping("/create-user")
    public ResponseEntity<String> create(@RequestBody UserRequest userRequest){
        return new ResponseEntity<>(userService.createUser(userRequest.getFullName(), userRequest.getEmail(), userRequest.getPassword(), userRequest.getType(), userRequest.getBalance()), CREATED);
    }

    @PostMapping("/login-user")
    public ResponseEntity<String> login(@RequestBody UserRequest userRequest){
        return new ResponseEntity<>(userService.login(userRequest.getEmail(), userRequest.getPassword()), OK);
    }

    @PatchMapping("/update-balance")
    public ResponseEntity<String> updateBalance(@RequestBody UpdateBalanceRequest updateBalanceRequest){
        System.out.println("ID: " + updateBalanceRequest.getId());
        System.out.println("Balance: " + updateBalanceRequest.getBalance());
        return new ResponseEntity<>(userService.updateBalance(updateBalanceRequest.getId(), updateBalanceRequest.getBalance()), OK);
    }

    @PostMapping("/validate-user")
    public ResponseEntity<Map<String, String>> validateUser(@RequestBody ValidateUserRequest validateUserRequest){
        return new ResponseEntity<>(userService.validateUser(validateUserRequest.getOtp(), validateUserRequest.getEmail()), OK);
    }
}

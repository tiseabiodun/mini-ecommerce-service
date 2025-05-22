package dev.tise.ecommerce.service.impl;

import dev.tise.ecommerce.model.User;
import dev.tise.ecommerce.repository.UserRepository;
import dev.tise.ecommerce.security.JwtTokenGenerator;
import dev.tise.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static dev.tise.ecommerce.utility.Constants.ACTIVE;
import static dev.tise.ecommerce.utility.Constants.PENDING;

@Service
public class UserServiceImpl implements UserService {

    @Value("${otp.generate}")
    private String otpGenerateUrl;

    @Value("${otp.validate}")
    private String otpValidateUrl;

    @Autowired
    private UserRepository userRepository;


    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenGenerator jwtTokenGenerator;

    public UserServiceImpl(PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtTokenGenerator jwtTokenGenerator) {
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    @Value("${jwt.token.expiry}")
    private long expiry;

    @Value("${jwt.refresh.expiry}")
    private long refresh;


    ArrayList<String> types = new ArrayList<>(Arrays.asList("ADMIN", "CUSTOMER"));

    @Override
    public String createUser(String fullName, String email, String password, String type, double balance) {

        System.err.println("Start of creation");
        try {
            Optional<User> optionalEmail = userRepository.findByEmail(email);
            if (optionalEmail.isPresent()) {
                throw new RuntimeException("Email already exists");
            }

            if (fullName.trim().length() < 5) {
                throw new RuntimeException("Full name does not have enough characters");
            }
            if (password.length() < 8) {
                throw new RuntimeException("Password is not strong enough, not enough characters");
            }
            if (!types.contains(type.trim().toUpperCase())) {
                throw new RuntimeException("Invalid type");
            }
            if (balance < 1000) {
                throw new RuntimeException("Balance is less than 1000.00");
            }
            System.err.println("After constraints");


            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setType(type);
            newUser.setPassword(encoder.encode(password));
            newUser.setBalance(balance);
            newUser.setStatus(PENDING);


            otpGenerateUrl = otpGenerateUrl.replace("[EMAIL]", email);
            otpGenerateUrl = otpGenerateUrl.replace("[ACCT]", fullName);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(otpGenerateUrl, new HashMap<>(), String.class);
            System.err.println("Response from OTP generation :: " + response);

            if (response != null && response.getStatusCode().is2xxSuccessful()) {

                userRepository.save(newUser);
                return "User created, kindly validate account in your email with OTP ";

            } else {
                return "Failed to generate OTP for customer";
            }


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public Map<String, String> validateUser(String otp, String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (email == null || email.trim().isEmpty()) {
            return Map.of("error", "Email is required");
        }
        if (optionalUser.isEmpty()) {
            return Map.of("error", "Customer with email " + email + " does not exist");
        }

        Map<String, String> details = new HashMap<>();
        details.put("token", otp);
        details.put("email", email);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(otpValidateUrl, details, String.class);
        System.out.println("Response from validate otp :: " + response);

        if (Objects.equals(response.getBody(), "Token has been validated")) {

            User user = optionalUser.get();
            user.setStatus(ACTIVE);
            userRepository.save(user);

            System.err.println("Success in saving the customer account ::" + optionalUser);

            return (Map.of("Success", "User has been validated"));

        }

        return Map.of("Error", "Unable to create user");
    }


    @Override
    public String login(String email, String password) {
        UsernamePasswordAuthenticationToken authToken;
        try {
            authToken = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authToken);

            if (authentication.isAuthenticated()) {

                Optional<User> optionalUser = userRepository.findByEmail(email);
                if (optionalUser.isEmpty()) {
                    throw new RuntimeException("This email does not exist");
                }

//                var loginDetails = optionalUser.get();
//                System.err.println(loginDetails.getEmail());
//                System.err.println(loginDetails.getPassword());
//                System.err.println(password);
//
//                if (!loginDetails.getPassword().equals(password)) {
//                    throw new RuntimeException("Sorry, Incorrect Password, please try again");
//                }
                System.err.println("Conigured token expiry time :: "+expiry);
                String normalToken = jwtTokenGenerator.generateToken(email, expiry);
                String refreshToken = jwtTokenGenerator.generateToken(email, refresh);
                return "Login Successful, Your token is + " + normalToken + "\nRefresh token:: " + refreshToken;
            }
            else{
                throw new RuntimeException("Sorry, not authorized");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String updateBalance(Long id, double amount) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("This user does not exist!");
            }

            if (amount < 100) {
                throw new RuntimeException("Sorry, amount is less than 100");
            }
            User user = optionalUser.get();
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            return "Balance has been updated successfully!";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

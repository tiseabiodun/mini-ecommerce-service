package dev.tise.ecommerce.service;

import java.util.Map;

public interface UserService {
    String createUser(String fullName,
                      String email,
                      String password,
                      String type,
                      double balance);
    Map<String, String> validateUser(String otp, String email);
    String login(String email, String password);
    String updateBalance(Long id, double amount);

}

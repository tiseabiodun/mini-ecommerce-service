package dev.tise.ecommerce.service;

public interface ProductService {
    String addProduct(String name, double price, String description);
    String removeProduct(Long id);
    String updateProduct(Long id, double price, String description);
}

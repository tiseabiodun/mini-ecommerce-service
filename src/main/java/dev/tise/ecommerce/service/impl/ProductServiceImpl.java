package dev.tise.ecommerce.service.impl;

import dev.tise.ecommerce.model.Product;
import dev.tise.ecommerce.repository.ProductRepository;
import dev.tise.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public String addProduct(String name, double price, String description) {
        try {
            if (name.trim().length() < 2) {
                throw new RuntimeException("Not a valid Product Name");
            }
            if (price < 0) {
                throw new RuntimeException("Price cannot be negative");
            }
            if (description.trim().isEmpty()) {
                throw new RuntimeException("Please add a description");
            }

            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setPrice(price);
            newProduct.setDescription(description);

            productRepository.save(newProduct);

            return "Product Successfully Added";
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String removeProduct(Long id) {
        try{
            Optional<Product> optionalProduct = productRepository.findById(id);

            if(optionalProduct.isEmpty()){
                throw new RuntimeException("Product does not exist");
            }

            productRepository.deleteById(id);

            return "Product has been deleted successfully";

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String updateProduct(Long id, double price, String description) {
        try{
            Optional<Product> optionalProduct = productRepository.findById(id);

            if(optionalProduct.isEmpty()){
                throw new RuntimeException("Product does not exist");
            }
            if (price < 0) {
                throw new RuntimeException("Price cannot be negative");
            }
            if (description.trim().isEmpty()) {
                throw new RuntimeException("Please add a description");
            }
            Product product = optionalProduct.get();

            product.setPrice(price);
            product.setDescription(description);

            productRepository.save(product);

            return "Product has been updated successfully";

    } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
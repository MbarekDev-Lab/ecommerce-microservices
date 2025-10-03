package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.ProductDto;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    public static final String PRODUCT_CACHE = "products";
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Caching(
            put = {@CachePut(value = PRODUCT_CACHE, key = "#result.getProductId()")},
            evict = {@CacheEvict(value = PRODUCT_CACHE, key = "'all'")}
    )
    public ProductDto createProduct(ProductDto productDto) {
        var product = new Product();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());

        Product savedProduct = productRepository.save(product);
        return new ProductDto(savedProduct.getId(), savedProduct.getName(),
                savedProduct.getPrice());
    }

    @Cacheable(value = PRODUCT_CACHE, key = "#productId")
    public ProductDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Cannot find product with id " + productId));
        return new ProductDto(product.getId(), product.getName(),
                product.getPrice());
    }

    @Cacheable(value = PRODUCT_CACHE, key = "'all'")
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> new ProductDto(product.getId(), product.getName(), product.getPrice()))
                .collect(Collectors.toList());
    }

    @Caching(
            put = {@CachePut(value = PRODUCT_CACHE, key = "#result.getProductId()")},
            evict = {@CacheEvict(value = PRODUCT_CACHE, key = "'all'")}
    )
    public ProductDto updateProduct(ProductDto productDto) {
        Long productId = productDto.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Cannot find product with id " + productId));

        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());

        Product updatedProduct = productRepository.save(product);
        return new ProductDto(updatedProduct.getId(), updatedProduct.getName(),
                updatedProduct.getPrice());
    }

    @Caching(
            evict = {
                    @CacheEvict(value = PRODUCT_CACHE, key = "#productId"),
                    @CacheEvict(value = PRODUCT_CACHE, key = "'all'")
            }
    )
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}

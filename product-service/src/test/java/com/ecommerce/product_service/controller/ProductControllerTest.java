package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.ProductDto;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_shouldReturnCreated() throws Exception {
        ProductDto productDto = new ProductDto(null, "Test Product", new BigDecimal("10.00"));
        ProductDto savedProductDto = new ProductDto(1L, "Test Product", new BigDecimal("10.00"));

        when(productService.createProduct(any(ProductDto.class))).thenReturn(savedProductDto);

        mockMvc.perform(post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void createProduct_whenInvalid_shouldReturnBadRequest() throws Exception {
        ProductDto productDto = new ProductDto(null, "", new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        ProductDto productDto = new ProductDto(1L, "Test Product", new BigDecimal("10.00"));

        when(productService.getProduct(1L)).thenReturn(productDto);

        mockMvc.perform(get("/api/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void getProduct_whenNotFound_shouldReturnNotFound() throws Exception {
        when(productService.getProduct(1L)).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/product/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts() throws Exception {
        List<ProductDto> products = List.of(
                new ProductDto(1L, "Product 1", new BigDecimal("10.00")),
                new ProductDto(2L, "Product 2", new BigDecimal("20.00"))
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].productId").value(2L));
    }

    @Test
    void updateProduct_shouldReturnOk() throws Exception {
        ProductDto productDto = new ProductDto(1L, "Updated Product", new BigDecimal("12.00"));

        when(productService.updateProduct(any(ProductDto.class))).thenReturn(productDto);

        mockMvc.perform(put("/api/product/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(12.00));
    }

    @Test
    void updateProduct_whenNotFound_shouldReturnNotFound() throws Exception {
        ProductDto productDto = new ProductDto(1L, "Updated Product", new BigDecimal("12.00"));

        when(productService.updateProduct(any(ProductDto.class))).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(put("/api/product/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/product/1"))
                .andExpect(status().isNoContent());
    }
}
